package de.hhn.abmi.service;

import de.hhn.abmi.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static de.hhn.abmi.service.OrderService.checkOrderQuantity;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The rules for a quantity the user types in, without Spring and without SAP. */
class OrderQuantityCheckTest {

    private static final BigDecimal PACKAGE = BigDecimal.TEN;
    private static final BigDecimal TARGET = BigDecimal.valueOf(30);

    @Test
    void vielfachesDerPackungIstErlaubt() {
        assertThatCode(() -> checkOrderQuantity(BigDecimal.valueOf(40), PACKAGE, TARGET)).doesNotThrowAnyException();
    }

    @Test
    void genauDasDoppelteDesZielbestandsIstErlaubt() {
        assertThatCode(() -> checkOrderQuantity(BigDecimal.valueOf(60), PACKAGE, TARGET)).doesNotThrowAnyException();
    }

    @Test
    void nullOderNegativWirdAbgelehnt() {
        assertThatThrownBy(() -> checkOrderQuantity(BigDecimal.ZERO, PACKAGE, TARGET))
                .isInstanceOf(ApiException.class).hasMessageContaining("greater than 0");
        assertThatThrownBy(() -> checkOrderQuantity(null, PACKAGE, TARGET))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void angebrochenePackungWirdAbgelehnt() {
        assertThatThrownBy(() -> checkOrderQuantity(BigDecimal.valueOf(25), PACKAGE, TARGET))
                .isInstanceOf(ApiException.class).hasMessageContaining("multiple of the package size 10");
    }

    @Test
    void tippfehlerUeberDemDoppeltenZielbestandWirdAbgelehnt() {
        assertThatThrownBy(() -> checkOrderQuantity(BigDecimal.valueOf(300), PACKAGE, TARGET))
                .isInstanceOf(ApiException.class).hasMessageContaining("must not exceed 60");
    }
}
