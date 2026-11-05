package de.hhn.abmi.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static de.hhn.abmi.service.OrderService.calculateOrderQuantity;
import static org.assertj.core.api.Assertions.assertThat;

/** The order formula on its own, without Spring and without SAP. */
class OrderQuantityTest {

    @Test
    void fuelltBisZumZielbestandAufUndRundetAufPackungenAuf() {
        // Enoxaparin on ward 3: 30 - 6 - 0 = 24, in packages of 10 -> 30
        assertThat(calculateOrderQuantity(n(6), n(30), n(0), n(10))).isEqualByComparingTo("30");
    }

    @Test
    void genauePackungsmengeWirdNichtAufgerundet() {
        assertThat(calculateOrderQuantity(n(10), n(30), n(0), n(10))).isEqualByComparingTo("20");
    }

    @Test
    void offeneBestellungWirdAbgezogen() {
        // 60 - 18 - 20 = 22, in packages of 20 -> 40
        assertThat(calculateOrderQuantity(n(18), n(60), n(20), n(20))).isEqualByComparingTo("40");
    }

    @Test
    void keineDoppelbestellungWennBereitsGenugBestelltIst() {
        assertThat(calculateOrderQuantity(n(6), n(30), n(30), n(10))).isEqualByComparingTo("0");
    }

    @Test
    void ohnePackungsgroesseWirdStueckgenauBestellt() {
        assertThat(calculateOrderQuantity(n(3), n(20), n(0), n(1))).isEqualByComparingTo("17");
    }

    private static BigDecimal n(int value) {
        return BigDecimal.valueOf(value);
    }
}
