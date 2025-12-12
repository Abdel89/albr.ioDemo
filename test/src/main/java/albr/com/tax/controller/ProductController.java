package albr.com.tax.controller;

import albr.com.tax.dto.ProductRequest;
import albr.com.tax.dto.ProductResponse;
import albr.com.tax.exception.ProductCreationException;
import albr.com.tax.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse addProduct(@RequestBody @Valid ProductRequest request) throws ProductCreationException {

        ProductResponse response = productService.add(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response).getBody();
    }


    @GetMapping("/{id}/final-price")
    public BigDecimal getFinalPrice(@PathVariable Long id) {
        return null;

    }

    @GetMapping("/{id}/")
    public ResponseEntity<ProductResponse> geProduct(@PathVariable Long id) {
        ProductResponse response = productService.getProduct(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}/final-price-tax-country")
    public ResponseEntity<ProductResponse> getFinalPriceTTC(@PathVariable Long id) {
        ProductResponse response = productService.getProductTTC(id);
        return ResponseEntity.ok(response);
    }
}
