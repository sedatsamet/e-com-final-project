package org.sedatsamet.orderservice.service;

import org.sedatsamet.orderservice.dto.PlaceOrderRequest;
import org.sedatsamet.orderservice.entity.Cart;
import org.sedatsamet.orderservice.entity.CartItem;
import org.sedatsamet.orderservice.entity.Order;
import org.sedatsamet.orderservice.repository.OrderRepository;
import org.sedatsamet.orderservice.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    private Order createNewOrder(PlaceOrderRequest request) {
        try {
            Cart cart = getCartByUserId(request);
            if(cart.getCartId().equals(request.getCartId()) && cart.getUserId().equals(orderUtil.getAuthenticatedUser().getUserId())) {
                Order order = Order.builder()
                        .userId(request.getUserId())
                        .totalPrice(cart.getTotalPrice())
                        .totalAmountOfProduct(calculateTotalAmountOfProducts(cart.getProducts()))
                        .transactionApproved(false)
                        .productIdList(cart.getProducts().stream().map(CartItem::getProductId).toList())
                        .build();
                return orderRepository.save(order);
            }else {
                throw new UsernameNotFoundException("You don't have an acess");
            }
        }catch (UsernameNotFoundException e){
            throw new RuntimeException("You don't have an acess");
        }catch (Exception e){
            throw new RuntimeException("Cart items not found");
        }
    }

    private Boolean checkAmountOfProduct(PlaceOrderRequest request) {
        boolean isEnough = false;
        List<CartItem> cartCartItems = getProductListOfCart(request);
        CartItem cartItemFromDb;
        for (CartItem cartItemInCart : cartCartItems) {
            cartItemFromDb = getCartItemFromProductService(cartItemInCart);
            if (cartItemInCart.getQuantity() < cartItemFromDb.getQuantity()) {
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
            cart = getCartByUserId(request);
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

    private CartItem getCartItemFromProductService(CartItem cartItem) {
        return orderUtil.getCartItemFromProductService(cartItem.getProductId());
    }

    private Cart getCartByUserId(PlaceOrderRequest request) {
        return orderUtil.getCartByUserId(request.getUserId());
    }

    private Double calculateTotalAmountOfProducts(List<CartItem> cartItems) {
        Double totalAmountOfProducts = 0.0;
        for (CartItem cartItem : cartItems) {
            totalAmountOfProducts += cartItem.getQuantity();
        }
        return totalAmountOfProducts;
    }
}
