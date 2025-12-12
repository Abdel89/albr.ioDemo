package albr.com.tax.enumTax;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Country {

    FRANCE("EUR"),
    CANADA("UCD"),
    USA("USD");

    private final String currency;

}
