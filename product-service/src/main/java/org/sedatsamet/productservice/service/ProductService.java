package org.sedatsamet.productservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.productservice.dto.request.CreateProductRequest;
import org.sedatsamet.productservice.dto.request.UpdateProductRequest;
import org.sedatsamet.productservice.entity.Product;
import org.sedatsamet.productservice.repository.ProductRepository;
import org.sedatsamet.productservice.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    /**
     * Retrieves a product from the repository based on the provided product ID.
     *
     * @param productId The ID of the product to retrieve.
     * @return The retrieved Product object if found, or null if not found.
     */
    public Product getProduct(UUID productId) {
        try {
            // Attempt to find the product in the repository using the provided product ID
            Optional<Product> product = productRepository.findById(productId);
            return product.orElse(null);
        } catch (Exception e) {
            // Log the error and return null if any exception occurs during retrieval
            log.error("Error retrieving product with ID {}: {}", productId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Updates a product in the repository based on the provided update request.
     *
     * @param createProductRequest The request containing the updated product details.
     * @return ResponseEntity containing the updated Product object if successful, or a 401 status if the product is not found.
     */
    public ResponseEntity<Product> updateProduct(UpdateProductRequest createProductRequest) {
        try {
            // Attempt to find the product in the repository using the provided product ID
            Optional<Product> product = productRepository.findById(createProductRequest.getProductId());

            if (product.isPresent()) {
                Product updatedProduct = product.get();
                updatedProduct.setName(createProductRequest.getName());
                updatedProduct.setDescription(createProductRequest.getDescription());
                updatedProduct.setQuantity(createProductRequest.getQuantity());
                updatedProduct.setPrice(createProductRequest.getPrice());

                // Check if an image is provided in the request and update the product image accordingly
                if (createProductRequest.getImage() != null) {
                    updatedProduct.setProductImage(ImageUtils.compressImage(createProductRequest.getImage().getBytes()));
                }

                // Save the updated product and return it with a 200 OK status
                return ResponseEntity.ok(productRepository.save(updatedProduct));
            }

            // If product is not found, return a 401 status
            return ResponseEntity.status(401).build();

        } catch (Exception e) {
            // Log the error and return an appropriate response
            log.error("Error updating product with ID {}: {}", createProductRequest.getProductId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates and saves a new product in the repository based on the provided request.
     *
     * @param createProductRequest The request containing the details of the new product to be created.
     * @return The newly created Product object.
     */
    public Product createProduct(CreateProductRequest createProductRequest) {
        try {
            // Create a new Product instance and populate its fields using the provided request
            Product product = new Product();
            product.setName(createProductRequest.getName());
            product.setDescription(createProductRequest.getDescription());
            product.setQuantity(createProductRequest.getQuantity());
            product.setPrice(createProductRequest.getPrice());

            // Check if an image is provided in the request and set the product image accordingly
            if (createProductRequest.getImage() != null) {
                product.setProductImage(ImageUtils.compressImage(createProductRequest.getImage().getBytes()));
            }

            // Save the newly created product in the repository and return it
            return productRepository.save(product);

        } catch (Exception e) {
            // Log the error and handle it appropriately (e.g., return an error response, throw a custom exception, etc.)
            log.error("Error creating product: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create product", e);
        }
    }

    /**
     * Deletes a product from the repository based on the provided UUID.
     *
     * @param uuid The UUID of the product to be deleted.
     * @return A status message indicating the result of the deletion operation.
     */
    public String deleteProduct(UUID uuid) {
        try {
            // Retrieve the product with the given UUID
            Product product = getProduct(uuid);

            // If the product does not exist, return a message indicating that the product was not found
            if (product == null) {
                return "Product not found";
            }

            // Delete the product from the repository
            productRepository.delete(product);

            // Return a success message after successful deletion
            return "Product deleted successfully";

        } catch (Exception e) {
            // Log the error and handle it appropriately (e.g., return an error message, throw a custom exception, etc.)
            log.error("Error deleting product with UUID {}: {}", uuid, e.getMessage(), e);
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    /**
     * Updates the quantity of a product based on the provided product ID.
     * The quantity change can be positive (increase) or negative (decrease).
     *
     * @param productId The ID of the product to update.
     * @param quantity The quantity by which the product's current quantity should be updated.
     * @return A ResponseEntity indicating the result of the update operation.
     */
    public ResponseEntity<?> updateProductQuantity(String productId, Integer quantity) {
        try {
            // Retrieve the product with the given product ID
            Optional<Product> product = productRepository.findById(UUID.fromString(productId));

            // If the product exists, update its quantity and save it back to the repository
            if (product.isPresent()) {
                Product updatedProduct = product.get();
                updatedProduct.setQuantity(updatedProduct.getQuantity() + quantity);
                return ResponseEntity.ok(productRepository.save(updatedProduct));
            }

            // If the product does not exist, return an unauthorized status
            return ResponseEntity.status(401).build();

        } catch (Exception e) {
            // Log the error and handle it appropriately (e.g., return an error response, throw a custom exception, etc.)
            log.error("Error updating quantity for product with ID {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to update product quantity", e);
        }
    }
}
