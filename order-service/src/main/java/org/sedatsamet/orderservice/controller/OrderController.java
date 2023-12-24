package org.sedatsamet.orderservice.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.orderservice.dto.PlaceOrderRequest;
import org.sedatsamet.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/order")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * Endpoint to place an order.
     *
     * @param orderRequest The request containing details of the order to be placed.
     * @return ResponseEntity with the order details or an error message.
     */
    @PostMapping("/placeOrder")
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderRequest orderRequest) {
        try {
            return ResponseEntity.ok(orderService.placeOrder(orderRequest));
        } catch (Exception e) {
            // Log the error and return a response with an unauthorized status and error message
            log.error("Error in placeOrder: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Endpoint to approve an order by its ID.
     *
     * @param orderId The ID of the order to be approved.
     * @return ResponseEntity with the approved order details or an error message.
     */
    @PutMapping("/approveOrderByOrderId")
    public ResponseEntity<?> approveOrderByOrderId(@RequestParam String orderId) {
        try {
            return ResponseEntity.ok(orderService.approveOrder(orderId));
        } catch (NotFoundException e) {
            // Log the error and return a response with a not found status and error message
            log.error("Order not found while approving: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found while approving");
        } catch (Exception e) {
            // Log the error and return a response with an unauthorized status and error message
            log.error("Error while approving order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have an access");
        }
    }

    /**
     * Endpoint to deny an order by its ID.
     *
     * @param orderId The ID of the order to be denied.
     * @return ResponseEntity with the denied order details or an error message.
     */
    @PutMapping("/denyOrderByOrderId")
    public ResponseEntity<?> denyOrderByOrderId(@RequestParam String orderId) {
        try {
            return ResponseEntity.ok(orderService.denyOrder(orderId));
        } catch (NotFoundException e) {
            // Log the error and return a response with a not found status and error message
            log.error("Order not found while denying: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found while denying");
        } catch (Exception e) {
            // Log the error and return a response with an unauthorized status and error message
            log.error("Error while denying order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error denying order");
        }
    }

    /**
     * Endpoint to fetch orders by a user's ID.
     *
     * @param userId The ID of the user to fetch orders for.
     * @return ResponseEntity with the list of orders or an error message.
     */
    @GetMapping("/getOrdersByUserId")
    public ResponseEntity<?> getOrdersByUserId(@RequestParam String userId) {
        try {
            return ResponseEntity.ok(orderService.getOrderByUserId(UUID.fromString(userId)));
        } catch (NotFoundException e) {
            // Log the error and return a response with a not found status and error message
            log.error("Order not found while fetching by user ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        } catch (Exception e) {
            // Log the error and return a response with an unauthorized status and error message
            log.error("Error while fetching orders by user ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error fetching orders");
        }
    }

    /**
     * Endpoint to fetch all orders.
     *
     * @return ResponseEntity with the list of all orders or an error message.
     */
    @GetMapping("/getAllOrders")
    public ResponseEntity<?> getAllOrders() {
        try {
            return ResponseEntity.ok(orderService.getAllOrders());
        } catch (Exception e) {
            // Log the error and return a response with a conflict status and error message
            log.error("Error while fetching all orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error fetching all orders");
        }
    }
}
