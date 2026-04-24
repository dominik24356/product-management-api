package com.productapi.product;

import com.productapi.exception.ProductNotFoundException;
import com.productapi.product.dto.CreateProductRequest;
import com.productapi.product.dto.ProductResponse;
import com.productapi.product.dto.UpdateProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private final ProductMapper productMapper = new ProductMapperImpl();

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, productMapper);
    }

    @Test
    void create_shouldReturnProductResponse_whenValidRequest() {
        // given
        CreateProductRequest request = new CreateProductRequest(
                "Laptop", "Opis", new BigDecimal("1999.99"), "Elektronika"
        );
        ProductEntity savedEntity = ProductEntity.builder()
                .id(1L)
                .name("Laptop")
                .description("Opis")
                .price(new BigDecimal("1999.99"))
                .category("Elektronika")
                .build();

        when(productRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

        // when
        ProductResponse result = productService.create(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Laptop");
        assertThat(result.price()).isEqualByComparingTo("1999.99");
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void getById_shouldReturnProductResponse_whenProductExists() {
        // given
        ProductEntity entity = ProductEntity.builder()
                .id(1L)
                .name("Laptop")
                .description("Opis")
                .price(new BigDecimal("1999.99"))
                .category("Elektronika")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));

        // when
        ProductResponse result = productService.getById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Laptop");
    }

    @Test
    void getById_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");

        verify(productRepository).findById(99L);
    }

    @Test
    void getAll_shouldReturnPageOfProducts_whenProductsExist() {
        // given
        ProductEntity entity = ProductEntity.builder()
                .id(1L)
                .name("Laptop")
                .description("Opis")
                .price(new BigDecimal("1999.99"))
                .category("Elektronika")
                .build();
        Page<ProductEntity> productPage = new PageImpl<>(List.of(entity));

        when(productRepository.findAll(PageRequest.of(0, 20))).thenReturn(productPage);

        // when
        Page<ProductResponse> result = productService.getAll(0, 20);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Laptop");
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoProductsExist() {
        // given
        when(productRepository.findAll(PageRequest.of(0, 20)))
                .thenReturn(Page.empty());

        // when
        Page<ProductResponse> result = productService.getAll(0, 20);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void getByCategory_shouldReturnProducts_whenCategoryExists() {
        // given
        ProductEntity entity = ProductEntity.builder()
                .id(1L)
                .name("Laptop")
                .description("Opis")
                .price(new BigDecimal("1999.99"))
                .category("Elektronika")
                .build();

        when(productRepository.findByCategory("Elektronika")).thenReturn(List.of(entity));

        // when
        List<ProductResponse> result = productService.getByCategory("Elektronika");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).category()).isEqualTo("Elektronika");
    }

    @Test
    void getByCategory_shouldReturnEmptyList_whenCategoryDoesNotExist() {
        // given
        when(productRepository.findByCategory("Nieistniejaca")).thenReturn(List.of());

        // when
        List<ProductResponse> result = productService.getByCategory("Nieistniejaca");

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void update_shouldUpdateOnlyProvidedFields_whenPartialUpdateRequest() {
        // given
        ProductEntity entity = ProductEntity.builder()
                .id(1L)
                .name("Stara nazwa")
                .description("Stary opis")
                .price(new BigDecimal("999.99"))
                .category("Elektronika")
                .build();
        UpdateProductRequest request = new UpdateProductRequest(
                "Nowa nazwa", null, null, null
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(entity);

        // when
        ProductResponse result = productService.update(1L, request);

        // then
        assertThat(result.name()).isEqualTo("Nowa nazwa");
        assertThat(result.description()).isEqualTo("Stary opis");
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void update_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // given
        UpdateProductRequest request = new UpdateProductRequest(
                "Nowa nazwa", null, null, null
        );
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.update(99L, request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_shouldDeleteProduct_whenProductExists() {
        // given
        when(productRepository.existsById(1L)).thenReturn(true);

        // when
        productService.delete(1L);

        // then
        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // given
        when(productRepository.existsById(99L)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");

        verify(productRepository, never()).deleteById(any());
    }
}