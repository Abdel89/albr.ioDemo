package albr.com.tax.service;

import albr.com.tax.repository.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("USA")
public class TaxStraegyUSAImp implements TaxStrategy {


    private static final BigDecimal RATE = new BigDecimal("0.07864");
    private static final BigDecimal FIXED_FEE = new BigDecimal("3");

    @Override
    public BigDecimal calculateTax(Product product) {
        return product.getPrice().multiply(RATE).add(FIXED_FEE);

    }

    @Override
    public BigDecimal calculateFinalPriceTTC(Product product) {
        return product.getPrice().add(calculateTax(product));
    }
}

