package albr.com.service;

import albr.com.tax.enumTax.Country;
import albr.com.tax.repository.Product;
import albr.com.tax.service.TaxService;
import albr.com.tax.service.TaxStrategy;
import albr.com.tax.service.TaxStrategyResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxServiceTest {

    @Mock
    private TaxStrategyResolver resolver;
    @Mock
    private TaxStrategy strategy;
    @Mock
    private Product product;

    @InjectMocks
    private TaxService service;

    @Test
    void findTaxStrategy_throwsIllegalArgument_whenProductIsNull() {
        assertThatThrownBy(() -> service.findTaxStrategy(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product must not be null");
        verifyNoInteractions(resolver);
    }

    @Test
    void findTaxStrategy_delegatesToResolver_withCountryName() {
        // given
        when(product.getCountry()).thenReturn(Country.FRANCE);
        when(resolver.resolve("FRANCE")).thenReturn(strategy);

        // when
        TaxStrategy result = service.findTaxStrategy(product);

        // then
        assertThat(result).isSameAs(strategy);
        verify(resolver).resolve("FRANCE");
        verifyNoMoreInteractions(resolver);
    }

    @Test
    void findTaxStrategy_propagatesResolverException_whenCountryUnknown() {
        when(product.getCountry()).thenReturn(Country.USA);
        when(resolver.resolve("USA"))
                .thenThrow(new IllegalArgumentException("No TaxStrategy found for country=USA"));

        assertThatThrownBy(() -> service.findTaxStrategy(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("country=USA");

        verify(resolver).resolve("USA");
    }

    @Test
    void findTaxStrategy_nullCountry_throwsNPE_dueToNameCall() {
        when(product.getCountry()).thenReturn(null);

        assertThatThrownBy(() -> service.findTaxStrategy(product))
                .isInstanceOf(NullPointerException.class); // .name() on null

        verifyNoInteractions(resolver);
    }
}
