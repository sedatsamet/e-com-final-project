package org.sedatsamet.orderservice.controller;

import org.sedatsamet.orderservice.dto.PlaceOrderRequest;
import org.sedatsamet.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/placeOrder")
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderRequest orderRequest) {
        try {
            return ResponseEntity.ok(orderService.placeOrder(orderRequest));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/getOrdersByUserId")
    public ResponseEntity<?> getOrdersByUserId(@RequestParam String userId) {
        try {
            return ResponseEntity.ok(orderService.getOrderByUserId(UUID.fromString(userId)));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/getAllOrders")
    public ResponseEntity<?> getAllOrders(){
        try {
            return ResponseEntity.ok(orderService.getAllOrders());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
