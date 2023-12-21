package org.sedatsamet.orderservice.service;

import jakarta.ws.rs.NotFoundException;
import org.sedatsamet.orderservice.dto.PlaceOrderRequest;
import org.sedatsamet.orderservice.entity.Cart;
import org.sedatsamet.orderservice.entity.CartItem;
import org.sedatsamet.orderservice.entity.Order;
import org.sedatsamet.orderservice.entity.User;
import org.sedatsamet.orderservice.repository.OrderRepository;
import org.sedatsamet.orderservice.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private OrderUtil orderUtil;
    @Autowired
    private OrderRepository orderRepository;

    public ResponseEntity<?> placeOrder(PlaceOrderRequest placeOrderRequest) {
        if(orderUtil.isUserAdmin()){
            return checkAmountOfProduct(placeOrderRequest) ? ResponseEntity.ok(createNewOrder(placeOrderRequest)) :
                    ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
        }else {
            if(orderUtil.getAuthenticatedUser().getUserId().equals(placeOrderRequest.getUserId())){
                return checkAmountOfProduct(placeOrderRequest) ? ResponseEntity.ok(createNewOrder(placeOrderRequest)) :
                        ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
            }else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have an acess");
            }
        }
    }

    public Order approveOrder(String orderId) {
        if(orderUtil.isUserAdmin()){
            Order approvedOrder = orderRepository.findById(UUID.fromString(orderId)).orElse(null);
            if(approvedOrder != null && approvedOrder.getTransactionApproved() == false){
                try {
                    approvedOrder.setTransactionApproved(true);
                    Cart cart = getCartByUserId(approvedOrder.getUserId());
                    for(CartItem cartItem : cart.getProducts()){
                        orderUtil.setUpdateProductQuantity(cartItem.getProductId(), -cartItem.getQuantity());
                    }
                }catch (Exception e){
                    throw new RuntimeException("Product not found while increasing quantities of product with orderId: " + approvedOrder.getOrderId());
                }
                return orderRepository.save(approvedOrder);
            }else{
                throw new NotFoundException("Order not found while approving");
            }
        }else{
            throw new RuntimeException("You don't have an acess");
        }
    }

    public Order denyOrder(String orderId){
        if(orderUtil.isUserAdmin()){
            Order approvedOrder = orderRepository.findById(UUID.fromString(orderId)).orElse(null);
            if(approvedOrder != null){
                approvedOrder.setTransactionApproved(false);
                try {
                    Cart cart = getCartByUserId(approvedOrder.getUserId());
                    for(CartItem cartItem : cart.getProducts()){
                        orderUtil.setUpdateProductQuantity(cartItem.getProductId(), +cartItem.getQuantity());
                    }
                }catch (Exception e){
                    throw new RuntimeException("Product not found while increasing quantities of product with orderId: " + approvedOrder.getOrderId());
                }
                return orderRepository.save(approvedOrder);
            }else{
                throw new NotFoundException("Order not found while denying");
            }
        }else{
            throw new RuntimeException("You don't have an acess");
        }
    }

    public List<Order> getAllOrders() {
        if(orderUtil.isUserAdmin()){
            try {
                return orderRepository.findAll();
            }catch (Exception e){
                throw new RuntimeException("Order not found");
            }
        }else {
            throw new RuntimeException("You don't have an acess");
        }
    }

    public List<Order> getOrderByUserId(UUID userId){
        if(orderUtil.isUserAdmin()){
            try {
                return orderRepository.findAllByUserId(userId);
            }catch (Exception e){
                throw new RuntimeException("Order not found");
            }
        }else {
            if(orderUtil.getAuthenticatedUser().getUserId().equals(userId)){
                try {
                    return orderRepository.findAllByUserId(userId);
                }catch (Exception e){
                    throw new RuntimeException("Order not found");
                }
            }else {
                throw new RuntimeException("You don't have an acess");
            }
        }
    }

    private Order createNewOrderFinal(Order order, UUID userId) {
        User updatedUser = updateUserOrderIdList(userId, order.getOrderId());
        orderUtil.updateUserOrderIdList(updatedUser);
        return orderRepository.save(order);
    }

    private Order createNewOrder(PlaceOrderRequest request) {
        Order order;
        Cart cart;
        try {
            cart = getCartByUserId(request.getUserId());
            if(cart.getCartId().equals(request.getCartId()) && cart.getUserId().equals(orderUtil.getAuthenticatedUser().getUserId())) {
                order = Order.builder()
                        .orderId(UUID.randomUUID())
                        .userId(request.getUserId())
                        .totalPrice(cart.getTotalPrice())
                        .totalAmountOfProduct(calculateTotalAmountOfProducts(cart.getProducts()))
                        .transactionApproved(false)
                        .productIdList(cart.getProducts().stream().map(CartItem::getProductId).toList())
                        .build();
            }else {
                throw new UsernameNotFoundException("You don't have an acess");
            }
        }catch (UsernameNotFoundException e){
            throw new RuntimeException("You don't have an acess");
        }catch (Exception e){
            throw new RuntimeException("Cart items not found");
        }
        return createNewOrderFinal(order, request.getUserId());
    }

    private Boolean checkAmountOfProduct(PlaceOrderRequest request) {
        boolean isEnough = false;
        List<CartItem> cartCartItems = getProductListOfCart(request);
        CartItem cartItemFromDb;
        for (CartItem cartItemInCart : cartCartItems) {
            cartItemFromDb = getCartItemFromProductService(cartItemInCart);
            if (cartItemInCart.getQuantity() <= cartItemFromDb.getQuantity()) {
                isEnough = true;
            }else{
                isEnough = false;
            }
        }
        return isEnough;
    }

    private List<CartItem> getProductListOfCart(PlaceOrderRequest request) {
        Cart cart;
        List<CartItem> cartItems = new ArrayList<>();
        try {
            cart = getCartByUserId(request.getUserId());
            if(cart.getCartId().equals(request.getCartId()) && cart.getUserId().equals(orderUtil.getAuthenticatedUser().getUserId())) {
                cartItems = cart.getProducts();
            }else{
                throw new UsernameNotFoundException("You don't have an acess");
            }
        }catch (UsernameNotFoundException e){
            throw new RuntimeException("You don't have an acess");
        }catch (Exception e){
            throw new RuntimeException("Cart items not found");
        }
        return cartItems;
    }

    private Cart getCartByUserId(UUID userId) {
        return orderUtil.getCartByUserId(userId);
    }

    private Double calculateTotalAmountOfProducts(List<CartItem> cartItems) {
        Double totalAmountOfProducts = 0.0;
        for (CartItem cartItem : cartItems) {
            totalAmountOfProducts += cartItem.getQuantity();
        }
        return totalAmountOfProducts;
    }

    private User updateUserOrderIdList(UUID userId, UUID orderId) {
        User user = orderUtil.getUserDetailsByUserId(userId);
        List<UUID> orderIdListOfUser = user.getOrderIdList();
        orderIdListOfUser.add(orderId);
        user.setOrderIdList(orderIdListOfUser);
        return user;
    }

    private CartItem getCartItemFromProductService(CartItem cartItem) {
        return orderUtil.getCartItemFromProductService(cartItem.getProductId());
    }
}
