package albr.com.service;

import albr.com.tax.service.TaxStrategy;
import albr.com.tax.service.TaxStrategyResolver;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Tests unitaires pour TaxStrategyResolver.
 */
class TaxStrategyResolverTest {

    @Test
    void resolve_returnsStrategy_whenCountryExists() {
        TaxStrategy fr = mock(TaxStrategy.class);
        TaxStrategy ca = mock(TaxStrategy.class);

        Map<String, TaxStrategy> map = new HashMap<>();
        map.put("FRANCE", fr);
        map.put("CANADA", ca);

        TaxStrategyResolver resolver = new TaxStrategyResolver(map);

        assertThat(resolver.resolve("FRANCE")).isSameAs(fr);
        assertThat(resolver.resolve("CANADA")).isSameAs(ca);
    }

    @Test
    void resolve_throwsIllegalArgument_whenCountryNotFound() {
        Map<String, TaxStrategy> map = Map.of("FRANCE", mock(TaxStrategy.class));
        TaxStrategyResolver resolver = new TaxStrategyResolver(map);

        assertThatThrownBy(() -> resolver.resolve("USA"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("USA");
    }

    @Test
    void resolve_throwsIllegalArgument_whenCountryIsNull() {
        Map<String, TaxStrategy> map = new HashMap<>();
        TaxStrategyResolver resolver = new TaxStrategyResolver(map);

        assertThatThrownBy(() -> resolver.resolve(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("country=null");
    }

    @Test
    void resolve_isCaseSensitive_byDefault() {
        TaxStrategy fr = mock(TaxStrategy.class);
        Map<String, TaxStrategy> map = Map.of("FRANCE", fr);
        TaxStrategyResolver resolver = new TaxStrategyResolver(map);

        // "france" n'existe pas si la clé a été stockée "FRANCE"
        assertThatThrownBy(() -> resolver.resolve("france"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("france");
    }
}
