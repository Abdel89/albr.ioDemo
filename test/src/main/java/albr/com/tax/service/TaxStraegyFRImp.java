package albr.com.tax.service;

import albr.com.tax.repository.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("FRANCE")
public class TaxStraegyFRImp implements TaxStrategy {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    @Override
    public BigDecimal calculateTax(Product product) {
        return product.getPrice().multiply(RATE);
    }

    @Override
    public BigDecimal calculateFinalPriceTTC(Product product) {
        return product.getPrice().add(calculateTax(product));
    }
}

