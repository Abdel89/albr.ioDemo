package albr.com.service;

import albr.com.tax.dto.ProductRequest;
import albr.com.tax.dto.ProductResponse;
import albr.com.tax.exception.ProductCreationException;
import albr.com.tax.mapper.ProductMapper;
import albr.com.tax.repository.Product;
import albr.com.tax.repository.ProductRepository;
import albr.com.tax.service.ProductService;
import albr.com.tax.service.TaxService;
import albr.com.tax.service.TaxStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private TaxService taxService;
    @Mock
    private TaxStrategy taxStrategy;

    @InjectMocks
    private ProductService service;

    @BeforeEach
    void setup() {
        // Rien de spécial : @InjectMocks crée l'instance avec les mocks
    }

    // ---------------- add(request) ----------------

    @Nested
    class AddProduct {

        @Test
        void shouldThrowNPE_whenRequestIsNull() {
            assertThatThrownBy(() -> service.add(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("ProductRequest must not be null");
        }

        @Test
        void shouldThrowIllegalArgument_whenNegativePrice() {
            ProductRequest req = mock(ProductRequest.class);
            Product mapped = mock(Product.class);

            when(productMapper.toEntity(req)).thenReturn(mapped);
            // price.signum() < 0
            when(mapped.getPrice()).thenReturn(new BigDecimal("-1"));

            assertThatThrownBy(() -> service.add(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product price cannot be negative");

            verify(repository, never()).save(any());
            verify(productMapper, never()).toResponse(any());
        }


        @Test
        void shouldMapSaveAndReturnResponse_onHappyPath() throws Exception, ProductCreationException {
            ProductRequest req = mock(ProductRequest.class);
            Product mapped = mock(Product.class);
            Product saved = mock(Product.class);
            ProductResponse resp = mock(ProductResponse.class);

            when(productMapper.toEntity(req)).thenReturn(mapped);
            when(mapped.getPrice()).thenReturn(new BigDecimal("99.99"));
            when(repository.save(mapped)).thenReturn(saved);
            when(productMapper.toResponse(saved)).thenReturn(resp);

            ProductResponse result = service.add(req);

            assertThat(result).isSameAs(resp);
            verify(productMapper).toEntity(req);
            verify(repository).save(mapped);
            verify(productMapper).toResponse(saved);
        }
    }

    // ---------------- getProduct(id) ----------------

    @Nested
    class GetProduct {

        @Test
        void shouldThrowNPE_whenIdIsNull() {
            assertThatThrownBy(() -> service.getProduct(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Product id must not be null");
        }

        @Test
        void shouldThrowIllegalArgument_whenNotFound() {
            Long id = 42L;
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getProduct(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product not found with id=" + id);
        }

        @Test
        void shouldMapFoundEntity_toResponse() {
            Long id = 7L;
            Product entity = mock(Product.class);
            ProductResponse resp = mock(ProductResponse.class);

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(productMapper.toResponse(entity)).thenReturn(resp);

            ProductResponse result = service.getProduct(id);

            assertThat(result).isSameAs(resp);
            verify(productMapper).toResponse(entity);
        }
    }

    // ---------------- getProductTTC(id) ----------------

    @Nested
    class GetProductTTC {

        @Test
        void shouldThrowNPE_whenIdIsNull() {
            assertThatThrownBy(() -> service.getProductTTC(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Product id must not be null");
        }

        @Test
        void shouldThrowIllegalArgument_whenNotFound() {
            Long id = 99L;
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getProductTTC(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product not found with id=" + id);
        }

        @Test
        void shouldComputeTax_mapResponse_andSetFinalPrice() {
            Long id = 5L;
            Product product = mock(Product.class);
            when(repository.findById(id)).thenReturn(Optional.of(product));

            // strategy & tax
            when(taxService.findTaxStrategy(product)).thenReturn(taxStrategy);
            BigDecimal total = new BigDecimal("123.45");
            when(taxStrategy.calculateTax(product)).thenReturn(total);

            // mapping & final price setter
            ProductResponse response = mock(ProductResponse.class);
            when(productMapper.toResponse(product)).thenReturn(response);

            ProductResponse result = service.getProductTTC(id);

            assertThat(result).isSameAs(response);
            verify(response).setFinalPrice(total);
            verify(taxService).findTaxStrategy(product);
            verify(taxStrategy).calculateTax(product);
            verify(productMapper).toResponse(product);
        }
    }
}
