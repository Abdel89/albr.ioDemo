package albr.com.service;

import albr.com.tax.repository.Product;
import albr.com.tax.service.TaxStraegyCANImp;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class TaxStraegyCANImpTest {

    private final TaxStraegyCANImp strategy = new TaxStraegyCANImp();

    @Test
    void tax_withoutExtraFee_whenPriceAtOrBelowThreshold() {
        // price == 10.00 -> taxe = 1.00, pas de frais
        Product p1 = mock(Product.class);
        when(p1.getPrice()).thenReturn(new BigDecimal("10.00"));

        BigDecimal tax = strategy.calculateTax(p1);
        BigDecimal total = strategy.calculateFinalPriceTTC(p1);

        assertThat(tax).isEqualByComparingTo("1.00");
        assertThat(total).isEqualByComparingTo("11.00");

        // price < 10.00 -> taxe simple
        Product p2 = mock(Product.class);
        when(p2.getPrice()).thenReturn(new BigDecimal("9.99"));

        assertThat(strategy.calculateTax(p2)).isEqualByComparingTo("0.999");
        assertThat(strategy.calculateFinalPriceTTC(p2)).isEqualByComparingTo("10.989");
    }

    @Test
    void tax_withExtraFee_whenPriceAboveThreshold() {
        // juste au-dessus du seuil: 10.01 -> 10% = 1.001 + 10 = 11.001
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(new BigDecimal("10.01"));

        BigDecimal tax = strategy.calculateTax(p);
        BigDecimal total = strategy.calculateFinalPriceTTC(p);

        assertThat(tax).isEqualByComparingTo("11.001");
        assertThat(total).isEqualByComparingTo("21.011");
    }

    @Test
    void largePrice_example() {
        // 100 -> 10% = 10 + 10 = 20
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(new BigDecimal("100"));

        assertThat(strategy.calculateTax(p)).isEqualByComparingTo("20");
        assertThat(strategy.calculateFinalPriceTTC(p)).isEqualByComparingTo("120");
    }

    @Test
    void decimalPrice_noRoundingInsideStrategy() {
        // 19.99 -> 10% = 1.999 + 10 = 11.999
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(new BigDecimal("19.99"));

        BigDecimal tax = strategy.calculateTax(p);
        BigDecimal total = strategy.calculateFinalPriceTTC(p);

        assertThat(tax).isEqualByComparingTo("11.999");
        assertThat(total).isEqualByComparingTo("31.989");
    }

    @Test
    void zeroPrice_edgeCase() {
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(BigDecimal.ZERO);

        assertThat(strategy.calculateTax(p)).isEqualByComparingTo("0.0"); // 0 * 0.10
        assertThat(strategy.calculateFinalPriceTTC(p)).isEqualByComparingTo("0.0");
    }

    @Test
    void negativePrice_behavesAsIs_noExtraFee() {
        // -5 -> 10% = -0.5 ; comme -5 <= 10, pas de frais ; TTC = -5.5
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(new BigDecimal("-5"));

        assertThat(strategy.calculateTax(p)).isEqualByComparingTo("-0.5");
        assertThat(strategy.calculateFinalPriceTTC(p)).isEqualByComparingTo("-5.5");
    }

    @Test
    void nullProduct_throwsNPE() {
        assertThrows(NullPointerException.class, () -> strategy.calculateTax(null));
        assertThrows(NullPointerException.class, () -> strategy.calculateFinalPriceTTC(null));
    }

    @Test
    void nullPrice_throwsNPE() {
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> strategy.calculateTax(p));
        assertThrows(NullPointerException.class, () -> strategy.calculateFinalPriceTTC(p));
    }
}
