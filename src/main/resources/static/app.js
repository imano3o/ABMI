// MediFlow frontend without a framework.
// Talks only to our own REST API, never to SAP.

const API = "/api/medications";
const ORDERS_API = "/api/orders";

const STATUS_TEXT = { OK: "OK", LOW: "Niedrig", CRITICAL: "Kritisch" };

const tbody = document.getElementById("medications");
const messageBox = document.getElementById("message");
const stationSelect = document.getElementById("station");
const STATION_KEY = "mediflow.station";

function currentStation() {
    return stationSelect.value;
}

function withStation(url) {
    return `${url}?station=${encodeURIComponent(currentStation())}`;
}

// ---------- Helpers ----------

/** Calls the API. On errors, throws with the message from the server. */
async function request(url, options = {}) {
    let response;
    try {
        response = await fetch(url, {
            headers: { "Content-Type": "application/json" },
            ...options,
        });
    } catch (e) {
        throw new Error("Backend nicht erreichbar. Läuft die App auf Port 8080?");
    }

    const body = await response.json().catch(() => null);
    if (!response.ok) {
        throw new Error(body?.message ?? `Fehler ${response.status}`);
    }
    return body;
}

function showMessage(text, type) {
    messageBox.textContent = text;
    messageBox.className = `message ${type}`;
    messageBox.hidden = false;
}

function hideMessage() {
    messageBox.hidden = true;
}

function formatNumber(value) {
    return Number(value).toLocaleString("de-DE");
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text ?? "";
    return div.innerHTML;
}

// ---------- Hospital ----------

async function loadHospital() {
    try {
        const hospital = await request("/api/hospital");
        if (hospital.name) {
            document.getElementById("hospital").textContent = hospital.name;
            document.title = `MediFlow – ${hospital.name}`;
        }
    } catch (e) {
        // Not critical: the default title stays
    }
}

// ---------- Stations ----------

async function loadStations() {
    const stations = await request("/api/stations");
    stationSelect.innerHTML = stations
        .map((s) => `<option value="${escapeHtml(s.code)}">${escapeHtml(s.name)}</option>`)
        .join("");

    let saved = null;
    try { saved = localStorage.getItem(STATION_KEY); } catch (e) { /* storage may be blocked, not critical */ }
    if (saved && stations.some((s) => s.code === saved)) {
        stationSelect.value = saved;
    }
}

stationSelect.addEventListener("change", () => {
    try { localStorage.setItem(STATION_KEY, currentStation()); } catch (e) { /* storage may be blocked, not critical */ }
    hideMessage();
    loadAll();
});

// ---------- List ----------

async function loadMedications() {
    try {
        const medications = await request(withStation(API));
        renderTable(medications);
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty">Keine Daten</td></tr>`;
        showMessage(e.message, "error");
    }
}

function renderTable(medications) {
    if (medications.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty">Für diese Station sind keine Medikamente hinterlegt</td></tr>`;
        return;
    }

    // Most urgent first
    const order = { CRITICAL: 0, LOW: 1, OK: 2 };
    medications.sort((a, b) => order[a.status] - order[b.status]);

    tbody.innerHTML = medications.map(renderRow).join("");
}

function renderRow(m) {
    const material = escapeHtml(m.material);

    return `
        <tr>
            <td>${escapeHtml(m.name)}<div class="material">${material}</div></td>
            <td class="num">${formatNumber(m.stock)} ${escapeHtml(m.unit)}</td>
            <td class="num">${formatNumber(m.minimumStock)}</td>
            <td><span class="badge ${m.status}">${STATUS_TEXT[m.status] ?? m.status}</span></td>
            <td>
                <form data-action="consume" data-material="${material}">
                    <input type="number" name="quantity" min="1" value="1" required aria-label="Menge">
                    <input type="text" name="note" class="note" maxlength="60" placeholder="Notiz (optional)" aria-label="Notiz">
                    <button type="submit">Entnehmen</button>
                </form>
            </td>
        </tr>
    `;
}

// ---------- Orders ----------

const proposalsBody = document.getElementById("proposals");
const ordersBody = document.getElementById("orders");
const placeOrdersButton = document.getElementById("place-orders");

async function loadOrders() {
    try {
        const [proposals, orders] = await Promise.all([
            request(withStation(`${ORDERS_API}/proposals`)),
            request(withStation(ORDERS_API)),
        ]);
        renderProposals(proposals);
        renderOrders(orders);
    } catch (e) {
        proposalsBody.innerHTML = `<tr><td colspan="7" class="empty">Keine Daten</td></tr>`;
        ordersBody.innerHTML = `<tr><td colspan="6" class="empty">Keine Daten</td></tr>`;
        showMessage(e.message, "error");
    }
}

function renderProposals(proposals) {
    placeOrdersButton.disabled = !proposals.some((p) => p.proposedQuantity > 0);
    if (proposals.length === 0) {
        proposalsBody.innerHTML = `<tr><td colspan="7" class="empty">Alle Bestände sind in Ordnung</td></tr>`;
        return;
    }
    proposalsBody.innerHTML = proposals.map((p) => `
        <tr>
            <td>${escapeHtml(p.name)} <span class="badge ${p.status}">${STATUS_TEXT[p.status] ?? p.status}</span>
                <div class="material">${escapeHtml(p.material)}</div></td>
            <td class="num">${formatNumber(p.stock)}</td>
            <td class="num">${formatNumber(p.targetStock)}</td>
            <td class="num">${formatNumber(p.openOrderQuantity)}</td>
            <td class="num">${formatNumber(p.packageSize)}</td>
            <td class="num"><strong>${p.proposedQuantity > 0
                ? `${formatNumber(p.proposedQuantity)} ${escapeHtml(p.unit)}`
                : "bereits bestellt"}</strong></td>
            <td>
                <form data-action="order" data-material="${escapeHtml(p.material)}">
                    <!-- Prefilled with the proposal; the backend checks the rules again -->
                    <input type="number" name="quantity" required aria-label="Bestellmenge"
                           value="${p.proposedQuantity > 0 ? p.proposedQuantity : p.packageSize}"
                           min="${p.packageSize}" step="${p.packageSize}" max="${p.targetStock * 2}">
                    <button type="submit">Bestellen</button>
                </form>
            </td>
        </tr>
    `).join("");
}

function renderOrders(orders) {
    if (orders.length === 0) {
        ordersBody.innerHTML = `<tr><td colspan="6" class="empty">Noch keine Bestellungen</td></tr>`;
        return;
    }
    ordersBody.innerHTML = orders.map((o) => `
        <tr>
            <td>${escapeHtml(o.purchaseOrder)}</td>
            <td>${escapeHtml(o.material)}</td>
            <td class="num">${formatNumber(o.quantity)} ${escapeHtml(o.unit)}</td>
            <td>${new Date(o.orderDate).toLocaleDateString("de-DE")}</td>
            <td><span class="badge ${o.delivered ? "OK" : "LOW"}">${o.delivered ? "Geliefert" : "Offen"}</span></td>
            <td>${o.delivered ? "" : `<button type="button" data-receive="${escapeHtml(o.purchaseOrder)}">Wareneingang buchen</button>`}</td>
        </tr>
    `).join("");
}

placeOrdersButton.addEventListener("click", async () => {
    hideMessage();
    placeOrdersButton.disabled = true;
    try {
        const created = await request(withStation(ORDERS_API), { method: "POST" });
        showMessage(
            created.length === 0
                ? "Es gibt nichts zu bestellen."
                : `${created.length} Bestellung(en) angelegt: ` +
                  created.map((o) => `${o.purchaseOrder} (${formatNumber(o.quantity)} × ${o.material})`).join(", "),
            "success"
        );
        await loadAll();
    } catch (e) {
        showMessage(e.message, "error");
        placeOrdersButton.disabled = false;
    }
});

// One listener for all order forms, because rows are re-rendered
proposalsBody.addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.target;
    const button = form.querySelector("button");
    const material = form.dataset.material;
    const quantity = Number(form.quantity.value);

    hideMessage();
    button.disabled = true;
    try {
        const order = await request(withStation(`${ORDERS_API}/${encodeURIComponent(material)}`), {
            method: "POST",
            body: JSON.stringify({ quantity }),
        });
        showMessage(`Bestellung ${order.purchaseOrder} angelegt: ${formatNumber(order.quantity)} ${order.unit} ${material}.`, "success");
        await loadAll();
    } catch (e) {
        showMessage(e.message, "error");
        button.disabled = false;
    }
});

ordersBody.addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-receive]");
    if (!button) return;
    const purchaseOrder = button.dataset.receive;

    hideMessage();
    button.disabled = true;
    try {
        const result = await request(
            withStation(`${ORDERS_API}/${encodeURIComponent(purchaseOrder)}/goods-receipt`), { method: "POST" });
        showMessage(
            `Wareneingang für Bestellung ${purchaseOrder} gebucht (Beleg ${result.materialDocument}): ` +
            `+${formatNumber(result.quantity)} ${result.unit} ${result.material}.`,
            "success"
        );
        await loadAll();
    } catch (e) {
        showMessage(e.message, "error");
        button.disabled = false;
    }
});

/** Stock, proposals and orders depend on each other, so they are always reloaded together. */
function loadAll() {
    return Promise.all([loadMedications(), loadOrders()]);
}

// ---------- Actions ----------

async function consume(material, form) {
    const quantity = Number(form.quantity.value);
    const note = form.note.value.trim() || null;

    const result = await request(withStation(`${API}/${encodeURIComponent(material)}/consume`), {
        method: "POST",
        body: JSON.stringify({ quantity, note }),
    });
    showMessage(
        `${quantity} × ${material} entnommen (Beleg ${result.materialDocument}). ` +
        `Restbestand: ${formatNumber(result.remainingStock)} ${result.unit}.`,
        "success"
    );
}

// One listener for all forms, because rows are re-rendered
tbody.addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.target;
    const button = form.querySelector("button");
    const material = form.dataset.material;

    hideMessage();
    button.disabled = true;
    try {
        await consume(material, form);
        await loadAll();
    } catch (e) {
        showMessage(e.message, "error");
        button.disabled = false;
    }
});

document.getElementById("reload").addEventListener("click", () => {
    hideMessage();
    loadAll();
});

// Stations must be loaded first, the medications depend on the selected station
async function init() {
    loadHospital();
    try {
        await loadStations();
    } catch (e) {
        showMessage(e.message, "error");
        return;
    }
    loadAll();
}

init();
