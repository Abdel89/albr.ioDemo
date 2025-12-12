package albr.com.tax.service;

import albr.com.tax.repository.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("CANADA")

public class TaxStraegyCANImp implements TaxStrategy {

    private static final BigDecimal RATE = new BigDecimal("0.10");
    private static final BigDecimal EXTRA_FEE = new BigDecimal("10");
    private static final BigDecimal THRESHOLD = new BigDecimal("10");


    @Override
    public BigDecimal calculateTax(Product product) {

        BigDecimal tax = product.getPrice().multiply(RATE);

        if (product.getPrice().compareTo(THRESHOLD) > 0) {
            tax = tax.add(EXTRA_FEE);
        }

        return tax;
    }


    @Override
    public BigDecimal calculateFinalPriceTTC(Product product) {
        return product.getPrice().add(calculateTax(product));
    }
}


