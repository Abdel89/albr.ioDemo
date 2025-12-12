package albr.com.tax.mapper;

import albr.com.tax.dto.ProductRequest;
import albr.com.tax.dto.ProductResponse;
import albr.com.tax.repository.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Request -> Entity
    @Mappings({
            @Mapping(target = "id", ignore = true)  // calculé ailleurs
    })
    Product toEntity(ProductRequest request);

    // Entity -> Response
    @Mappings({
            @Mapping(target = "basePrice", source = "price"),
            @Mapping(target = "currency", expression = "java(product.getCountry() != null ? product.getCountry().getCurrency() : null)"),
            @Mapping(target = "finalPrice", ignore = true)  // calculé ailleurs
    })
    ProductResponse toResponse(Product product);
}
