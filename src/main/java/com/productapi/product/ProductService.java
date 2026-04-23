package com.productapi.product;

import com.productapi.exception.ProductNotFoundException;
import com.productapi.product.dto.CreateProductRequest;
import com.productapi.product.dto.ProductResponse;
import com.productapi.product.dto.UpdateProductRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse create(CreateProductRequest request) {
        ProductEntity product = productMapper.toEntity(request);
        return productMapper.toResponse(productRepository.save(product));
    }

    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Page<ProductResponse> getAll(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size))
                .map(productMapper::toResponse);
    }

    public List<ProductResponse> getByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse update(Long id, UpdateProductRequest request) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productMapper.updateEntity(request, product);

        return productMapper.toResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }
}
