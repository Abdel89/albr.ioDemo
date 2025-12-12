package albr.com.tax.repository;

import albr.com.tax.enumTax.Country;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private Country country;

    public Product() {
    }

    public Product(String name, BigDecimal price, Country country) {
        this.name = name;
        this.price = price;
        this.country = country;
    }


}
