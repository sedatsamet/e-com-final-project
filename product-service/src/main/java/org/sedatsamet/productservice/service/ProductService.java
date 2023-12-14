package org.sedatsamet.productservice.service;

import org.sedatsamet.productservice.dto.request.CreateProductRequest;
import org.sedatsamet.productservice.entity.Product;
import org.sedatsamet.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public Product createProduct(CreateProductRequest createProductRequest) throws IOException {
        Product product = new Product();
        product.setName(createProductRequest.getName());
        product.setDescription(createProductRequest.getDescription());
        product.setQuantity(createProductRequest.getQuantity());
        if(createProductRequest.getImage() != null) {
            product.setProductImage(createProductRequest.getImage().getBytes());
        }
        return productRepository.save(product);
    }
}
