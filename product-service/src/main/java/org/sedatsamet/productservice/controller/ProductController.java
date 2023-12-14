package org.sedatsamet.productservice.controller;

import org.sedatsamet.productservice.dto.request.CreateProductRequest;
import org.sedatsamet.productservice.entity.Product;
import org.sedatsamet.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@ModelAttribute CreateProductRequest createProductRequest) {
        try {
            return ResponseEntity.created(null).body(productService.createProduct(createProductRequest));
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }

    }
}
