package org.sedatsamet.productservice.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.productservice.dto.request.CreateProductRequest;
import org.sedatsamet.productservice.dto.request.UpdateProductRequest;
import org.sedatsamet.productservice.entity.Product;
import org.sedatsamet.productservice.service.ProductService;
import org.sedatsamet.productservice.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/product")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
    @Autowired
    private ProductService productService;

    /**
     * Retrieves a product based on the provided product ID.
     *
     * @param productId The ID of the product to retrieve.
     * @return ResponseEntity containing the product if found, or a not found response if not.
     */
    @GetMapping("/getProductById")
    public ResponseEntity<Product> getProduct(@RequestParam String productId) {
        try {
            // Attempt to fetch the product using the provided product ID
            Product product = productService.getProduct(UUID.fromString(productId));

            // If product is not found, return a 404 Not Found response
            if (product == null) {
                return ResponseEntity.notFound().build();
            }

            // If product is found, return it with a 200 OK response
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            // In case of any exceptions, log the error and return a 500 Internal Server Error response
            log.error("Error fetching product with ID {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates a product based on the provided update request.
     *
     * @param updateProductRequest The request containing the details to update the product.
     * @return ResponseEntity containing the updated product if successful, or a not found response if not.
     */
    @PutMapping("/update")
    public ResponseEntity<Product> updateProduct(@ModelAttribute UpdateProductRequest updateProductRequest) {
        try {
            // Attempt to update the product using the provided request
            return productService.updateProduct(updateProductRequest);
        } catch (Exception e) {
            // If there's an IO exception (or any other unexpected error), return a 404 Not Found response
            log.error("Error updating product: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Creates a new product based on the provided creation request.
     *
     * @param createProductRequest The request containing the details to create the product.
     * @return ResponseEntity containing the newly created product if successful, or a not found response if not.
     */
    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@ModelAttribute CreateProductRequest createProductRequest) {
        try {
            // Attempt to create the product using the provided request
            return ResponseEntity.created(null).body(productService.createProduct(createProductRequest));
        } catch (Exception e) {
            // If there's an IO exception (or any other unexpected error), return a 404 Not Found response
            log.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves and serves the image associated with a given product ID.
     *
     * @param productId The ID of the product whose image is to be retrieved.
     * @return ResponseEntity containing the image data if successful, or an appropriate error response if not.
     */
    @GetMapping("/productImage/{productId}")
    public ResponseEntity<?> downloadImage(@PathVariable String productId) {
        try {
            // Retrieve the image data for the specified product
            byte[] imageData = productService.getProduct(UUID.fromString(productId)).getProductImage();
            // Return the image data with appropriate content type
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf("image/png"))
                    .body(ImageUtils.decompressImage(imageData));
        } catch (Exception e) {
            // Log the error and return a 404 Not Found response
            log.error("Error retrieving image for product with ID {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a product with the given product ID.
     *
     * @param productId The ID of the product to be deleted.
     * @return ResponseEntity indicating the success of the operation or an error response if unsuccessful.
     */
    @DeleteMapping("/deleteProduct")
    public ResponseEntity<?> deleteProduct(@RequestParam String productId) {
        try {
            // Attempt to delete the product using the provided service method
            return ResponseEntity.ok(productService.deleteProduct(UUID.fromString(productId)));
        } catch (Exception e) {
            // Log the error and return an appropriate error response
            log.error("Error deleting product with ID {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting product");
        }
    }

    /**
     * Updates the quantity of a product with the given product ID.
     *
     * @param productId The ID of the product whose quantity needs to be updated.
     * @param quantity  The new quantity value.
     * @return ResponseEntity indicating the success of the operation or an error response if unsuccessful.
     */
    @PutMapping("/updateProductQuantity")
    public ResponseEntity<?> updateProductQuantity(@RequestParam String productId, @RequestParam String quantity) {
        try {
            // Attempt to update the product quantity using the provided service method
            return productService.updateProductQuantity(productId, Integer.parseInt(quantity));
        } catch (NumberFormatException e) {
            // Log the error and return a bad request response if the quantity parameter is not a valid number
            log.error("Invalid quantity value provided for product with ID {}: {}", productId, quantity);
            return ResponseEntity.badRequest().body("Invalid quantity value");
        } catch (Exception e) {
            // Log the error and return a not found response if the product is not found or any other exception occurs
            log.error("Error updating quantity for product with ID {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
    }
}
