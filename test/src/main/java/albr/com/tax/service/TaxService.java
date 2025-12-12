package albr.com.tax.service;

import albr.com.tax.repository.Product;
import org.springframework.stereotype.Service;

@Service
public class TaxService {

    private final TaxStrategyResolver resolver;

    public TaxService(TaxStrategyResolver resolver) {
        this.resolver = resolver;
    }

    public TaxStrategy findTaxStrategy(Product product) {

        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        return resolver.resolve(product.getCountry().name());
    }
}
