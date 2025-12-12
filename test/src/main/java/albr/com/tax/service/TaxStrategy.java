package albr.com.tax.service;

import albr.com.tax.repository.Product;

import java.math.BigDecimal;

public interface TaxStrategy {

    /**
     * Calcule  le montant de la taxe
     */
    BigDecimal calculateTax(Product product);

    BigDecimal calculateFinalPriceTTC(Product product);

}
