package com.productapi.product;

import com.productapi.product.dto.CreateProductRequest;
import com.productapi.product.dto.ProductResponse;
import com.productapi.product.dto.UpdateProductRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductEntity toEntity(CreateProductRequest request);

    ProductResponse toResponse(ProductEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateProductRequest request, @MappingTarget ProductEntity entity);
}
