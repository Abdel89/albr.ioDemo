package albr.com.integration;

import albr.com.tax.dto.ProductRequest;
import albr.com.tax.dto.ProductResponse;
import albr.com.tax.enumTax.Country;
import albr.com.tax.exception.ProductCreationException;
import albr.com.tax.mapper.ProductMapper;
import albr.com.tax.repository.Product;
import albr.com.tax.repository.ProductRepository;
import albr.com.tax.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * IT "standalone" : pas de SpringBootTest, pas d'AutoConfigureMockMvc.
 * On câble :
 * - VRAI ProductMapper (MapStruct)
 * - VRAI TaxService + TaxStrategyResolver + Stratégies FR/CAN/USA
 * - Controller réel
 * - ProductRepository mocké (pas de DB)
 * Et on utilise MockMvcBuilders.standaloneSetup(...).
 */
class ProductApiStandaloneIT {

    private MockMvc mvc;

    // vrais composants
    private ProductMapper mapper;
    private TaxService taxService;

    // mocks
    private ProductRepository repository;

    // controller réel (à adapter à ton package/nom)
    private ProductController controller;

    @BeforeEach
    void setUp() {
        // 1) repo mock
        repository = Mockito.mock(ProductRepository.class);

        // 2) vrai mapper MapStruct
        mapper = Mappers.getMapper(ProductMapper.class);

        // 3) vraies stratégies + resolver + service
        Map<String, TaxStrategy> strategies = Map.of(
                "FRANCE", new TaxStraegyFRImp(),
                "CANADA", new TaxStraegyCANImp(),
                "USA", new TaxStraegyUSAImp()
        );
        TaxStrategyResolver resolver = new TaxStrategyResolver(strategies);
        taxService = new TaxService(resolver);

        // 4) vrai ProductService (repo mocké)
        ProductService productService = new ProductService(repository, mapper, taxService);

        // 5) contrôleur réel
        controller = new ProductController(productService);

        // 6) MockMvc standalone avec Jackson + advice
        var objectMapper = new ObjectMapper();
        mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new TestAdvice())
                .build();
    }

    // ----- Contrôleur REST minimal si tu n’en as pas (sinon supprime cette classe et importe le tien) -----
    @org.springframework.web.bind.annotation.RestController
    @org.springframework.web.bind.annotation.RequestMapping("/api/products")
    static class ProductController {
        private final ProductService service;

        ProductController(ProductService service) {
            this.service = service;
        }

        @org.springframework.web.bind.annotation.PostMapping
        public org.springframework.http.ResponseEntity<ProductResponse> add(
                @org.springframework.web.bind.annotation.RequestBody ProductRequest req
        ) throws ProductCreationException {
            ProductResponse resp = service.add(req);
            return org.springframework.http.ResponseEntity.created(URI.create("/api/products/" + resp.getId())).body(resp);
        }

        @org.springframework.web.bind.annotation.GetMapping("/{id}")
        public ProductResponse get(@org.springframework.web.bind.annotation.PathVariable Long id) {
            return service.getProduct(id);
        }

        @org.springframework.web.bind.annotation.GetMapping("/{id}/ttc")
        public ProductResponse getTtc(@org.springframework.web.bind.annotation.PathVariable Long id) {
            return service.getProductTTC(id);
        }
    }

    // ----- Advice pour mapper les exceptions -> HTTP (pour que les asserts 400/404 passent) -----
    @org.springframework.web.bind.annotation.RestControllerAdvice
    static class TestAdvice {
        @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
        public org.springframework.http.ResponseEntity<String> handleIAE(IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("Product not found")) {
                return new org.springframework.http.ResponseEntity<>(ex.getMessage(), NOT_FOUND);
            }
            return new org.springframework.http.ResponseEntity<>(ex.getMessage(), BAD_REQUEST);
        }

        @org.springframework.web.bind.annotation.ExceptionHandler(ProductCreationException.class)
        public org.springframework.http.ResponseEntity<String> handleCreation(ProductCreationException ex) {
            return new org.springframework.http.ResponseEntity<>(ex.getMessage(), BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("POST /api/products")
    class CreateProduct {

        @Test
        @DisplayName("201 Created + Location + body OK")
        void create_should_return201_and_body() throws Exception {
            // stub repo.save
            when(repository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                Product saved = new Product();
                saved.setId(1L);
                saved.setName(p.getName());
                saved.setPrice(p.getPrice());
                saved.setCountry(p.getCountry());
                return saved;
            });

            String json = """
                        { "name": "Keyboard", "price": 49.90, "country": "FRANCE" }
                    """;

            mvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/products/1")))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.basePrice").value(49.90))
                    .andExpect(jsonPath("$.currency").value("EUR"))
                    .andExpect(jsonPath("$.tax").doesNotExist())
                    .andExpect(jsonPath("$.finalPrice").doesNotExist());

            verify(repository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("400 Bad Request si prix négatif")
        void create_should_return400_on_negative_price() throws Exception {
            String json = """
                        { "name": "Bad", "price": -1, "country": "FRANCE" }
                    """;

            mvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetProduct {

        @Test
        @DisplayName("200 OK avec payload mapé")
        void get_should_return200_with_body() throws Exception {
            Product entity = new Product();
            entity.setId(7L);
            entity.setName("Mouse");
            entity.setPrice(new BigDecimal("10.00"));
            entity.setCountry(Country.CANADA);

            when(repository.findById(7L)).thenReturn(Optional.of(entity));

            mvc.perform(get("/api/products/7"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(7))
                    .andExpect(jsonPath("$.basePrice").value(10.00))
                    .andExpect(jsonPath("$.currency").value("CAD"))
                    .andExpect(jsonPath("$.finalPrice").doesNotExist());
        }

        @Test
        @DisplayName("404 Not Found si id inconnu")
        void get_should_return404_when_not_found() throws Exception {
            when(repository.findById(404L)).thenReturn(Optional.empty());

            mvc.perform(get("/api/products/404"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id}/ttc")
    class GetProductTtc {

        @Test
        @DisplayName("200 OK avec finalPrice calculé (FR)")
        void ttc_should_compute_fr() throws Exception {
            Product entity = new Product();
            entity.setId(5L);
            entity.setName("Headset");
            entity.setPrice(new BigDecimal("100.00"));
            entity.setCountry(Country.FRANCE);

            when(repository.findById(5L)).thenReturn(Optional.of(entity));

            mvc.perform(get("/api/products/5/ttc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.currency").value("EUR"))
                    .andExpect(jsonPath("$.basePrice").value(100.00))
                    .andExpect(jsonPath("$.finalPrice").value(120.00)); // 100 + 20%
        }

        @Test
        @DisplayName("200 OK avec règles Canada (seuil)")
        void ttc_should_compute_canada_threshold() throws Exception {
            Product entity = new Product();
            entity.setId(6L);
            entity.setName("Item");
            entity.setPrice(new BigDecimal("10.01"));
            entity.setCountry(Country.CANADA);

            when(repository.findById(6L)).thenReturn(Optional.of(entity));

            mvc.perform(get("/api/products/6/ttc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currency").value("CAD"))
                    .andExpect(jsonPath("$.finalPrice").value(21.011)); // 10.01*10% + 10 + base
        }

        @Test
        @DisplayName("404 Not Found si id inconnu (ttc)")
        void ttc_should_return404_when_not_found() throws Exception {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            mvc.perform(get("/api/products/999/ttc"))
                    .andExpect(status().isNotFound());
        }
    }
}
