package albr.com.service;

import albr.com.tax.repository.Product;
import albr.com.tax.service.TaxStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


abstract class TaxStrategyContractTest {

    protected abstract TaxStrategy strategy();

    protected Product productWithPrice(String price) {
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(new BigDecimal(price));
        return p;
    }

    @Test
    void nullProduct_throwsNPE_onCalculateTax_and_onCalculateFinalPriceTTC() {
        assertThrows(NullPointerException.class, () -> strategy().calculateTax(null));
        assertThrows(NullPointerException.class, () -> strategy().calculateFinalPriceTTC(null));
    }

    @Test
    void nullPrice_throwsNPE() {
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> strategy().calculateTax(p));
        assertThrows(NullPointerException.class, () -> strategy().calculateFinalPriceTTC(p));
    }

    @Test
    void coherence_finalPrice_equals_price_plus_tax() {
        Product p = productWithPrice("42.42");

        var s = strategy();
        BigDecimal tax = s.calculateTax(p);
        BigDecimal ttc = s.calculateFinalPriceTTC(p);

        assertThat(ttc).isEqualByComparingTo(p.getPrice().add(tax));
    }
}
