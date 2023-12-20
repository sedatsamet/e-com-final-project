package org.sedatsamet.cartservice.controller;

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
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/getCartByUserId")
    public ResponseEntity<?> getCart(@RequestParam String userId) {
        try {
            return ResponseEntity.ok(cartService.getCart(UUID.fromString(userId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have an acess");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody CartItemRequest request) {
        try {
            return cartService.addProduct(request);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have an acess");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Something went wrong");
        }
    }

    @PutMapping("/updateCartItem")
    public ResponseEntity<?> updateCartItem(@RequestBody CartItemRequest request) {
        try {
            return cartService.updateAmountOfCartItem(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/clearCart")
    public ResponseEntity<?> clearCart(@RequestParam String userId) {
        try {
            return ResponseEntity.ok(cartService.clearCart(UUID.fromString(userId)));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart not found while deleting all items");
        }
    }

    @GetMapping("/getCartByUserIdForOrderService")
    public ResponseEntity<Cart> getCartByCartId(@RequestParam String userId) {
        return ResponseEntity.ok(cartService.getCartByCartIdForOrderService(UUID.fromString(userId)));
    }
}
