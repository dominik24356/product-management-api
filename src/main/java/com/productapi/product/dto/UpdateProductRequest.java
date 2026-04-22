package com.productapi.product.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(

        @Size(max = 255)
        String name,

        @Size(max = 2000)
        String description,

        @PositiveOrZero
        BigDecimal price,

        @Size(max = 100)
        String category
) {
}
