package org.sedatsamet.cartservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.cartservice.dto.CartItemRequest;
import org.sedatsamet.cartservice.entity.Cart;
import org.sedatsamet.cartservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@Slf4j
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * Retrieves the cart for a given user ID.
     *
     * @param userId The UUID of the user.
     * @return ResponseEntity containing the cart details.
     */
    @GetMapping("/getCartByUserId")
    public ResponseEntity<?> getCart(@RequestParam String userId) {
        try {
            // Attempt to fetch and return the cart for the given user ID
            return ResponseEntity.ok(cartService.getCart(UUID.fromString(userId)));
        } catch (IllegalArgumentException e) {
            // Handle the case where the provided userId is not a valid UUID
            log.error("Invalid UUID provided: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID format");
        } catch (Exception e) {
            // Handle other unexpected exceptions
            log.error("Error fetching cart for user ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    /**
     * Endpoint to add a product to the cart.
     *
     * @param request The CartItemRequest containing product details.
     * @return ResponseEntity with the result of the operation.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody CartItemRequest request) {
        try {
            // Attempt to add the product using the cartService and return the result
            return cartService.addProduct(request);
        } catch (UsernameNotFoundException e) {
            // Handle the case where the user is not authorized
            log.error("User not authorized to add product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have access");
        } catch (Exception e) {
            // Handle other unexpected exceptions
            log.error("Error adding product to cart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }

    /**
     * Endpoint to update the amount of a cart item.
     *
     * @param request The CartItemRequest containing updated product details.
     * @return ResponseEntity with the result of the operation.
     */
    @PutMapping("/updateCartItem")
    public ResponseEntity<?> updateCartItem(@RequestBody CartItemRequest request) {
        try {
            // Attempt to update the cart item amount using the cartService and return the result
            return cartService.updateAmountOfCartItem(request);
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Error updating cart item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint to clear the cart for a specific user.
     *
     * @param userId The unique identifier for the user's cart to be cleared.
     * @return ResponseEntity with the result of the operation.
     */
    @DeleteMapping("/clearCart")
    public ResponseEntity<?> clearCart(@RequestParam String userId) {
        try {
            // Attempt to clear the cart using the cartService and return the result
            return ResponseEntity.ok(cartService.clearCart(UUID.fromString(userId)));
        } catch (UsernameNotFoundException e) {
            // Handle the case when the user's cart is not found
            log.warn("Cart not found for user with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart not found while deleting all items");
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Error clearing cart for user with ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint to retrieve a cart by user ID specifically designed for order service.
     *
     * @param userId The unique identifier for the user.
     * @return ResponseEntity containing the cart associated with the user.
     */
    @GetMapping("/getCartByUserIdForOrderService")
    public ResponseEntity<Cart> getCartByUserIdForOrderService(@RequestParam String userId) {
        try {
            // Attempt to retrieve the cart for the given user ID and return it
            return ResponseEntity.ok(cartService.getCartByCartIdForOrderService(UUID.fromString(userId)));
        } catch (IllegalArgumentException e) {
            // Handle the case when the provided UUID is not valid
            log.warn("Invalid UUID provided for user ID: {}", userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Error retrieving cart for user with ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
