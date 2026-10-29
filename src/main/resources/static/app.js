// MediFlow – einfaches Frontend ohne Framework.
// Spricht nur mit der eigenen REST-API (siehe API.md), kennt keine SAP-Begriffe.

const API = "/api/medications";

const STATUS_TEXT = { OK: "OK", LOW: "Niedrig", CRITICAL: "Kritisch" };

const tbody = document.getElementById("medications");
const messageBox = document.getElementById("message");
const stationSelect = document.getElementById("station");
const STATION_KEY = "mediflow.station";

/** Aktuell gewählte Station, z. B. "ST03". */
function currentStation() {
    return stationSelect.value;
}

/** Baut die URL mit ?station=… für die gewählte Station. */
function withStation(url) {
    return `${url}?station=${encodeURIComponent(currentStation())}`;
}

// ---------- Hilfsfunktionen ----------

/** Ruft die API auf und wirft bei Fehlern die message aus { status, message }. */
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

// ---------- Krankenhaus ----------

/** Zeigt den Krankenhausnamen im Kopf an, falls er gepflegt ist. */
async function loadHospital() {
    try {
        const hospital = await request("/api/hospital");
        if (hospital.name) {
            document.getElementById("hospital").textContent = hospital.name;
            document.title = `MediFlow – ${hospital.name}`;
        }
    } catch (e) {
        // Ohne Namen geht es auch, der Platzhalter bleibt stehen
    }
}

// ---------- Stationen ----------

/** Lädt die Stationen in die Auswahl und wählt die zuletzt benutzte wieder aus. */
async function loadStations() {
    const stations = await request("/api/stations");
    stationSelect.innerHTML = stations
        .map((s) => `<option value="${escapeHtml(s.code)}">${escapeHtml(s.name)}</option>`)
        .join("");

    let saved = null;
    try { saved = localStorage.getItem(STATION_KEY); } catch (e) { /* ohne Speicher geht es auch */ }
    if (saved && stations.some((s) => s.code === saved)) {
        stationSelect.value = saved;
    }
}

stationSelect.addEventListener("change", () => {
    try { localStorage.setItem(STATION_KEY, currentStation()); } catch (e) { /* egal */ }
    hideMessage();
    loadMedications();
});

// ---------- Liste ----------

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

    // Kritische zuerst, dann niedrige, dann OK
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

// ---------- Aktionen ----------

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

// Ein Listener für alle Formulare in der Tabelle
tbody.addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.target;
    const button = form.querySelector("button");
    const material = form.dataset.material;

    hideMessage();
    button.disabled = true;
    try {
        await consume(material, form);
        await loadMedications();
    } catch (e) {
        showMessage(e.message, "error");
        button.disabled = false;
    }
});

document.getElementById("reload").addEventListener("click", () => {
    hideMessage();
    loadMedications();
});

// Start: erst die Stationen laden, dann die Medikamente der gewählten Station
async function init() {
    loadHospital();
    try {
        await loadStations();
    } catch (e) {
        showMessage(e.message, "error");
        return;
    }
    loadMedications();
}

init();
