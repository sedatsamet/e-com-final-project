package org.sedatsamet.cartservice.controller;

import org.sedatsamet.cartservice.dto.CartItemRequest;
import org.sedatsamet.cartservice.dto.CartResponseDto;
import org.sedatsamet.cartservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;


    @GetMapping("/getCartByUserId")
    public ResponseEntity<CartResponseDto> getCart(@RequestParam String userId) {
        try{
            return ResponseEntity.ok(cartService.getCart(UUID.fromString(userId)));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponseDto> addProduct(@RequestBody CartItemRequest request) {
        return cartService.addProduct(request);
    }

    @PutMapping("/updateCartItem")
    public ResponseEntity<CartResponseDto> updateCartItem(@RequestBody CartItemRequest request) {
        try {
            return cartService.updateAmountOfCartItem(request);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
