package org.sedatsamet.orderservice.controller;

import jakarta.ws.rs.NotFoundException;
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

    @PutMapping("/approveOrderByOrderId")
    public ResponseEntity<?> approveOrderByOrderId(@RequestParam String orderId) {
        try {
            return ResponseEntity.ok(orderService.approveOrder(orderId));
        }catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found while approving");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have an access");
        }
    }

    @PutMapping("/denyOrderByOrderId")
    public ResponseEntity<?> denyOrderByOrderId(@RequestParam String orderId) {
        try {
            return ResponseEntity.ok(orderService.denyOrder(orderId));
        }catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/getOrdersByUserId")
    public ResponseEntity<?> getOrdersByUserId(@RequestParam String userId) {
        try {
            return ResponseEntity.ok(orderService.getOrderByUserId(UUID.fromString(userId)));
        }catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found while approving");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have an access");
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
