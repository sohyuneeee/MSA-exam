package com.sparta.msa_exam.product.application;

import com.sparta.msa_exam.product.api.ProductRequestDto;
import com.sparta.msa_exam.product.api.ProductResponseDto;
import com.sparta.msa_exam.product.config.RestPage;
import com.sparta.msa_exam.product.domain.Product;
import com.sparta.msa_exam.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @CachePut(cacheNames = "productCache", key = "#result.productId")
    public ProductResponseDto createProduct(String userId, ProductRequestDto requestDto) {
        Product product = productRepository.save(Product.of(requestDto.getName(), requestDto.getSupplyPrice(), Long.parseLong(userId)));
        return ProductResponseDto.from(product);
    }

    @Cacheable(cacheNames = "productListCache", key = "{args[0], args[1]}")
    public RestPage<ProductResponseDto> getProductList(int page, int size, String sortBy, boolean isAsc) {
        Pageable pageable = createPageableWithSorting(page, size, sortBy, isAsc);
        Page<Product> productPage = productRepository.findAll(pageable);
        Page<ProductResponseDto> productResponsePage = productPage.map(ProductResponseDto::from);
        return new RestPage<>(productResponsePage);
    }

    private Pageable createPageableWithSorting(int page, int size, String sortBy, boolean isAsc) {
        if (size != 10 && size != 30 && size != 50) {
            size = 10;
        }
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }

    public List<ProductResponseDto> getProductsByIdList(List<Long> productIdList) {
        List<Product> productList = productRepository.findByIdIn(productIdList);
        return productList.stream()
                .map(ProductResponseDto::from)
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "productCache", key = "methodName")
    public List<ProductResponseDto> getAllProduct() {
        List<Product> productList = productRepository.findAll();
       return productList.stream().map(ProductResponseDto::from).toList();
    }

}
