package albr.com.tax.service;

import albr.com.tax.dto.ProductRequest;
import albr.com.tax.dto.ProductResponse;
import albr.com.tax.exception.ProductCreationException;
import albr.com.tax.mapper.ProductMapper;
import albr.com.tax.repository.Product;
import albr.com.tax.repository.ProductRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;


@Service
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper productMapper;
    private final TaxService taxService;

    public ProductService(ProductRepository repository, ProductMapper productMapper, TaxService taxService) {
        this.repository = repository;
        this.productMapper = productMapper;
        this.taxService = taxService;
    }

    public ProductResponse add(ProductRequest request) throws ProductCreationException {

        requireNonNull(request, "ProductRequest must not be null");

        try {
            Product product = productMapper.toEntity(request);


            if (product.getPrice().signum() < 0) {
                throw new IllegalArgumentException("Product price cannot be negative");
            }

            Product savedProduct = repository.save(product);

            return productMapper.toResponse(savedProduct);

        } catch (DataAccessException ex) {
            // Exception technique → enveloppée
            throw new ProductCreationException("Unable to create product", ex);
        }
    }

    /* =========================
     GET PRODUCT + TAX
     ========================= */
    public ProductResponse getProduct(Long productId) {

        requireNonNull(productId, "Product id must not be null");

        Product product = repository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found with id=" + productId)
                );

        return productMapper.toResponse(product);

    }

    public ProductResponse getProductTTC(Long productId) {

        requireNonNull(productId, "Product id must not be null");

        // verifier existence produit
        Product productExist = repository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found with id=" + productId));

        TaxStrategy taxStrategy = taxService.findTaxStrategy(productExist);
        BigDecimal total = taxStrategy.calculateTax(productExist);
        // map product

        ProductResponse response = productMapper.toResponse(productExist);
        response.setFinalPrice(total);
        return response;
    }
}
