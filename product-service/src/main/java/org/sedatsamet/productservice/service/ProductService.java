package org.sedatsamet.productservice.service;

import org.sedatsamet.productservice.dto.request.CreateProductRequest;
import org.sedatsamet.productservice.dto.request.UpdateProductRequest;
import org.sedatsamet.productservice.entity.Product;
import org.sedatsamet.productservice.repository.ProductRepository;
import org.sedatsamet.productservice.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public Product getProduct(UUID productId) {
        Optional<Product> product = productRepository.findById(productId);
        return product.orElse(null);
    }

    public ResponseEntity<Product> updateProduct(UpdateProductRequest createProductRequest) throws IOException {
        Optional<Product> product = productRepository.findById(createProductRequest.getProductId());
        if(product.isPresent()){
            Product updatedProduct = product.get();
            updatedProduct.setName(createProductRequest.getName());
            updatedProduct.setDescription(createProductRequest.getDescription());
            updatedProduct.setQuantity(createProductRequest.getQuantity());
            updatedProduct.setPrice(createProductRequest.getPrice());
            if(createProductRequest.getImage() != null) {
                updatedProduct.setProductImage(ImageUtils.compressImage(createProductRequest.getImage().getBytes()));
            }
            return ResponseEntity.ok(productRepository.save(updatedProduct));
        }
        return ResponseEntity.status(401).build();
    }

    public Product createProduct(CreateProductRequest createProductRequest) throws IOException {
        Product product = new Product();
        product.setName(createProductRequest.getName());
        product.setDescription(createProductRequest.getDescription());
        product.setQuantity(createProductRequest.getQuantity());
        product.setPrice(createProductRequest.getPrice());
        if(createProductRequest.getImage() != null) {
            product.setProductImage(ImageUtils.compressImage(createProductRequest.getImage().getBytes()));
        }
        return productRepository.save(product);
    }

    public String deleteProduct(UUID uuid) {
        Product product = getProduct(uuid);
        if(product == null) {
            return "Product not found";
        }
        productRepository.delete(product);
        return "Product deleted successfully";
    }

    public ResponseEntity<?> updateProductQuantiy(String productId, Integer quantity) {
        Optional<Product> product = productRepository.findById(UUID.fromString(productId));
        if(product.isPresent()){
            Product updatedProduct = product.get();
            updatedProduct.setQuantity(updatedProduct.getQuantity() - quantity);
            return ResponseEntity.ok(productRepository.save(updatedProduct));
        }
        return ResponseEntity.status(401).build();
    }
}
