package albr.com.service;

import albr.com.tax.repository.Product;
import albr.com.tax.service.TaxStraegyFRImp;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour TaxStraegyFRImp (TVA 20%).
 */
class TaxStraegyFRImpTest {

    private final TaxStraegyFRImp strategy = new TaxStraegyFRImp();

    @Test
    void calculateTax_shouldReturn20Percent_ofPrice() {
        Product product = mock(Product.class);
        when(product.getPrice()).thenReturn(new BigDecimal("100.00"));

        BigDecimal tax = strategy.calculateTax(product);

        assertThat(tax).isEqualByComparingTo("20.00"); // 100 * 0.20
    }

    @Test
    void calculateFinalPriceTTC_shouldReturnPricePlusTax() {
        Product product = mock(Product.class);
        when(product.getPrice()).thenReturn(new BigDecimal("100.00"));

        BigDecimal total = strategy.calculateFinalPriceTTC(product);

        assertThat(total).isEqualByComparingTo("120.00");
        // Sanity: cohérence entre méthodes
        assertThat(total).isEqualByComparingTo(
                product.getPrice().add(strategy.calculateTax(product))
        );
    }

    @Test
    void zeroPrice_edgeCase() {
        Product product = mock(Product.class);
        when(product.getPrice()).thenReturn(BigDecimal.ZERO);

        assertThat(strategy.calculateTax(product)).isEqualByComparingTo("0.00");
        assertThat(strategy.calculateFinalPriceTTC(product)).isEqualByComparingTo("0.00");
    }

    @Test
    void decimalPrice_roundingIsDelegateToBigDecimalMath() {
        Product product = mock(Product.class);
        // 19.99 * 0.20 = 3.998 -> dépend de l’usage aval (pas d’arrondi dans la stratégie)
        when(product.getPrice()).thenReturn(new BigDecimal("19.99"));

        BigDecimal tax = strategy.calculateTax(product);
        BigDecimal total = strategy.calculateFinalPriceTTC(product);

        // Pas d’arrondi dans la stratégie : on vérifie la valeur exacte mathématique
        assertThat(tax).isEqualByComparingTo("3.998");
        assertThat(total).isEqualByComparingTo("23.988");
    }

    @Test
    void nullProduct_shouldThrowNPE() {
        assertThrows(NullPointerException.class, () -> strategy.calculateTax(null));
        assertThrows(NullPointerException.class, () -> strategy.calculateFinalPriceTTC(null));
    }

    @Test
    void nullPrice_shouldThrowNPE() {
        Product product = mock(Product.class);
        when(product.getPrice()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> strategy.calculateTax(product));
        assertThrows(NullPointerException.class, () -> strategy.calculateFinalPriceTTC(product));
    }
}
