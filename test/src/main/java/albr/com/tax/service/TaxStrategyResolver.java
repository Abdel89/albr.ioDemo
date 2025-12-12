package albr.com.tax.service;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TaxStrategyResolver {

    private final Map<String, TaxStrategy> strategiesByCountry;

    public TaxStrategyResolver(Map<String, TaxStrategy> strategiesByCountry) {
        this.strategiesByCountry = strategiesByCountry;
    }

    public TaxStrategy resolve(String country) {
        TaxStrategy strategy = strategiesByCountry.get(country);

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No TaxStrategy found for country=" + country
            );
        }

        return strategy;
    }
}
