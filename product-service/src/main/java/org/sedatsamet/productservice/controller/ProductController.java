package org.sedatsamet.productservice.controller;

import org.sedatsamet.productservice.dto.request.CreateProductRequest;
import org.sedatsamet.productservice.dto.request.UpdateProductRequest;
import org.sedatsamet.productservice.entity.Product;
import org.sedatsamet.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/get/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable UUID productId) {
        Product product = productService.getProduct(productId);
        if(product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PutMapping("/update")
    public ResponseEntity<Product> updateProduct(@ModelAttribute UpdateProductRequest updateProductRequest) {
        try {
            return productService.updateProduct(updateProductRequest);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@ModelAttribute CreateProductRequest createProductRequest) {
        try {
            return ResponseEntity.created(null).body(productService.createProduct(createProductRequest));
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }

    }
}
