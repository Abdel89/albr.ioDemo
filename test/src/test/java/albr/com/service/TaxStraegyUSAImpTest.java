package albr.com.service;

import albr.com.tax.repository.Product;
import albr.com.tax.service.TaxStraegyUSAImp;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour TaxStraegyUSAImp.
 * Règle: tax = price * 0.07864 + 3 ; TTC = price + tax.
 * Aucun arrondi appliqué dans la stratégie.
 */
class TaxStraegyUSAImpTest {

    private final TaxStraegyUSAImp strategy = new TaxStraegyUSAImp();

    @Test
    void calculateTax_nominal() {
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(new BigDecimal("100.00")); // 100*0.07864=7.864 + 3 = 10.864

        BigDecimal tax = strategy.calculateTax(p);
        assertThat(tax).isEqualByComparingTo("10.864");

        BigDecimal ttc = strategy.calculateFinalPriceTTC(p); // 100 + 10.864 = 110.864
        assertThat(ttc).isEqualByComparingTo("110.864");
    }

    @Test
    void zeroPrice_edgeCase() {
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(BigDecimal.ZERO); // 0*rate + 3 = 3

        assertThat(strategy.calculateTax(p)).isEqualByComparingTo("3");
        assertThat(strategy.calculateFinalPriceTTC(p)).isEqualByComparingTo("3");
    }

    @Test
    void decimalPrice_noRoundingInsideStrategy() {
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(new BigDecimal("19.99"));
        // 19.99 * 0.07864 = 1.571?:
        // 19.99 * 0.07864 = 1.571? exact BigDecimal multiplication:
        // On laisse BigDecimal faire; valeur attendue:
        // 19.99 * 0.07864 = 1.571?6 (précis: 1.571?  19.99*7864=157,? /100000 -> 1.571?6)
        // Calcul exact : 19.99 * 0.07864 = 1.571??6 -> 1.571?6
        // Pour éviter les erreurs de tête, on multiplie comme BigDecimal ferait:
        // 1999 * 7864 = 15_711_?  (calcul exact ci-dessous)
        // On peut simplement vérifier en reconstituant TTC = price + tax pour cohérence.
        BigDecimal tax = strategy.calculateTax(p);
        BigDecimal ttc = strategy.calculateFinalPriceTTC(p);

        // Valeurs exactes attendues (calculé proprement):
        // 19.99 * 0.07864 = 1.571??6 -> calcul précis:
        // 19.99 = 1999/100 ; 0.07864 = 7864/100000
        // (1999*7864) / 10^7
        // 1999*7864 = 1999*(8000-136) = 15_992_000 - 271_??4
        // Pour éviter toute imprécision ici, on valide avec relations algébriques exactes :
        assertThat(tax.subtract(new BigDecimal("3")) // tax - 3 = price * rate
        ).isEqualByComparingTo(p.getPrice().multiply(new BigDecimal("0.07864")));

        // On vérifie aussi quelques bornes raisonnables
        assertThat(tax).isGreaterThan(new BigDecimal("3.0"));     // car 19.99 * rate > 0
        assertThat(tax).isLessThan(new BigDecimal("5.0"));        // 19.99*0.07864 ≈ 1.57 ; +3 < 5
        assertThat(ttc).isEqualByComparingTo(p.getPrice().add(tax));
    }

    @Test
    void negativePrice_behavesAsFormula_allowsNegativeBase() {
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(new BigDecimal("-10")); // tax = -10*0.07864 + 3 = -0.7864 + 3 = 2.2136

        assertThat(strategy.calculateTax(p)).isEqualByComparingTo("2.2136");
        assertThat(strategy.calculateFinalPriceTTC(p)).isEqualByComparingTo("-7.7864");
    }

    @Test
    void largePrice_example() {
        Product p = mock(Product.class);
        when(p.getPrice()).thenReturn(new BigDecimal("1000")); // 1000*0.07864 + 3 = 78.64 + 3 = 81.64

        assertThat(strategy.calculateTax(p)).isEqualByComparingTo("81.64");
        assertThat(strategy.calculateFinalPriceTTC(p)).isEqualByComparingTo("1081.64");
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
