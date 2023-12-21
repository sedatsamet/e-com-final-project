package org.sedatsamet.orderservice.service;

import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderService {
    @Autowired
    private OrderUtil orderUtil;
    @Autowired
    private OrderRepository orderRepository;

    /**
     * Endpoint to place an order based on the request.
     *
     * @param placeOrderRequest The request containing order details.
     * @return ResponseEntity with the result of placing the order or an appropriate error message.
     */
    public ResponseEntity<?> placeOrder(PlaceOrderRequest placeOrderRequest) {
        try {
            if (orderUtil.isUserAdmin()) {
                return checkAmountOfProduct(placeOrderRequest) ? ResponseEntity.ok(createNewOrder(placeOrderRequest)) :
                        ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
            } else {
                if (orderUtil.getAuthenticatedUser().getUserId().equals(placeOrderRequest.getUserId())) {
                    return checkAmountOfProduct(placeOrderRequest) ? ResponseEntity.ok(createNewOrder(placeOrderRequest)) :
                            ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have an access");
                }
            }
        } catch (Exception e) {
            // Log the error and return a response with a conflict status and error message
            log.error("Error while placing order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error placing the order");
        }
    }

    /**
     * Approves an order based on the provided orderId.
     *
     * @param orderId The ID of the order to be approved.
     * @return The approved Order.
     * @throws NotFoundException if the order is not found.
     */
    public Order approveOrder(String orderId) {
        try {
            if (orderUtil.isUserAdmin()) {
                Order approvedOrder = orderRepository.findById(UUID.fromString(orderId)).orElse(null);
                if (approvedOrder != null && !approvedOrder.getTransactionApproved()) {
                    approvedOrder.setTransactionApproved(true);
                    Cart cart = getCartByUserId(approvedOrder.getUserId());
                    for (CartItem cartItem : cart.getProducts()) {
                        orderUtil.setUpdateProductQuantity(cartItem.getProductId(), -cartItem.getQuantity());
                    }
                    return orderRepository.save(approvedOrder);
                } else {
                    throw new NotFoundException("Order not found while approving");
                }
            } else {
                throw new RuntimeException("You don't have access");
            }
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error while approving order with ID {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Error approving the order");
        }
    }

    /**
     * Denies an order based on the provided orderId.
     *
     * @param orderId The ID of the order to be denied.
     * @return The denied Order.
     * @throws NotFoundException if the order is not found.
     */
    public Order denyOrder(String orderId) {
        try {
            if (orderUtil.isUserAdmin()) {
                Order deniedOrder = orderRepository.findById(UUID.fromString(orderId)).orElse(null);
                if (deniedOrder != null) {
                    deniedOrder.setTransactionApproved(false);
                    Cart cart = getCartByUserId(deniedOrder.getUserId());
                    for (CartItem cartItem : cart.getProducts()) {
                        orderUtil.setUpdateProductQuantity(cartItem.getProductId(), +cartItem.getQuantity());
                    }
                    return orderRepository.save(deniedOrder);
                } else {
                    throw new NotFoundException("Order not found while denying");
                }
            } else {
                throw new RuntimeException("You don't have access");
            }
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error while denying order with ID {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Error denying the order");
        }
    }

    /**
     * Retrieves all orders.
     *
     * @return List of all orders.
     * @throws RuntimeException if there's an error retrieving orders or if the user doesn't have access.
     */
    public List<Order> getAllOrders() {
        try {
            if (orderUtil.isUserAdmin()) {
                return orderRepository.findAll();
            } else {
                throw new RuntimeException("You don't have access");
            }
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error while retrieving all orders: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving all orders");
        }
    }

    /**
     * Retrieves orders for a given user ID.
     *
     * @param userId ID of the user.
     * @return List of orders for the specified user.
     * @throws RuntimeException if there's an error retrieving orders or if the user doesn't have access.
     */
    public List<Order> getOrderByUserId(UUID userId) {
        try {
            if (orderUtil.isUserAdmin()) {
                return orderRepository.findAllByUserId(userId);
            } else {
                if (orderUtil.getAuthenticatedUser().getUserId().equals(userId)) {
                    return orderRepository.findAllByUserId(userId);
                } else {
                    throw new RuntimeException("You don't have access");
                }
            }
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error while retrieving orders for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Error retrieving orders");
        }
    }

    /**
     * Creates a new order and updates user order ID list.
     *
     * @param order  The order to be created.
     * @param userId ID of the user.
     * @return The saved order.
     * @throws RuntimeException if there's an error creating the order or updating the user's order ID list.
     */
    private Order createNewOrderFinal(Order order, UUID userId) {
        try {
            User updatedUser = updateUserOrderIdList(userId, order.getOrderId());
            orderUtil.updateUserOrderIdList(updatedUser);
            return orderRepository.save(order);
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error while creating a new order for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Error creating a new order");
        }
    }

    /**
     * Creates a new order based on the given request.
     *
     * @param request The request containing order details.
     * @return The saved order.
     * @throws RuntimeException if there's an error creating the order or if the user does not have access.
     */
    private Order createNewOrder(PlaceOrderRequest request) {
        Order order;
        Cart cart;
        try {
            cart = getCartByUserId(request.getUserId());
            if (cart.getCartId().equals(request.getCartId()) && cart.getUserId().equals(orderUtil.getAuthenticatedUser().getUserId())) {
                order = Order.builder()
                        .orderId(UUID.randomUUID())
                        .userId(request.getUserId())
                        .totalPrice(cart.getTotalPrice())
                        .totalAmountOfProduct(calculateTotalAmountOfProducts(cart.getProducts()))
                        .transactionApproved(false)
                        .productIdList(cart.getProducts().stream().map(CartItem::getProductId).toList())
                        .build();
            } else {
                throw new UsernameNotFoundException("You don't have an access");
            }
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("You don't have an access");
        } catch (Exception e) {
            throw new RuntimeException("Cart items not found");
        }
        return createNewOrderFinal(order, request.getUserId());
    }

    /**
     * Checks if there's enough quantity of each product in the cart.
     *
     * @param request The request containing order details.
     * @return True if there's enough quantity for each product in the cart, otherwise false.
     * @throws RuntimeException if there's an error fetching product details or other unexpected issues.
     */
    private Boolean checkAmountOfProduct(PlaceOrderRequest request) {
        boolean isEnough = false;
        List<CartItem> cartCartItems = getProductListOfCart(request);
        CartItem cartItemFromDb;
        try {
            for (CartItem cartItemInCart : cartCartItems) {
                cartItemFromDb = getCartItemFromProductService(cartItemInCart);
                if (cartItemInCart.getQuantity() <= cartItemFromDb.getQuantity()) {
                    isEnough = true;
                } else {
                    isEnough = false;
                    break;  // Exit the loop as soon as one item is found to be insufficient
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error checking product quantity: " + e.getMessage());
        }
        return isEnough;
    }

    /**
     * Retrieves the list of products in the cart for the given user.
     *
     * @param request The request containing order details.
     * @return List of CartItem objects representing products in the cart.
     * @throws RuntimeException if there's an error retrieving cart details or other unexpected issues.
     */
    private List<CartItem> getProductListOfCart(PlaceOrderRequest request) {
        Cart cart;
        List<CartItem> cartItems = new ArrayList<>();
        try {
            cart = getCartByUserId(request.getUserId());
            if (cart.getCartId().equals(request.getCartId()) && cart.getUserId().equals(orderUtil.getAuthenticatedUser().getUserId())) {
                cartItems = cart.getProducts();
            } else {
                throw new RuntimeException("You don't have an access");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving cart items: " + e.getMessage());
        }
        return cartItems;
    }

    /**
     * Retrieves the cart for the given user ID.
     *
     * @param userId The ID of the user.
     * @return Cart object representing the user's cart.
     * @throws RuntimeException if there's an error retrieving the cart or other unexpected issues.
     */
    private Cart getCartByUserId(UUID userId) {
        try {
            return orderUtil.getCartByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving cart for user ID: " + userId + ". Reason: " + e.getMessage());
        }
    }

    /**
     * Calculates the total amount of products in the cart.
     *
     * @param cartItems List of cart items.
     * @return Total amount of products.
     * @throws RuntimeException if there's an error during calculation or other unexpected issues.
     */
    private Double calculateTotalAmountOfProducts(List<CartItem> cartItems) {
        try {
            Double totalAmountOfProducts = 0.0;
            for (CartItem cartItem : cartItems) {
                totalAmountOfProducts += cartItem.getQuantity();
            }
            return totalAmountOfProducts;
        } catch (Exception e) {
            throw new RuntimeException("Error calculating total amount of products. Reason: " + e.getMessage());
        }
    }

    /**
     * Updates the order ID list for a given user.
     *
     * @param userId The ID of the user whose order ID list needs to be updated.
     * @param orderId The ID of the order to be added to the user's order ID list.
     * @return Updated user object.
     * @throws RuntimeException if there's an error during the update or other unexpected issues.
     */
    private User updateUserOrderIdList(UUID userId, UUID orderId) {
        try {
            User user = orderUtil.getUserDetailsByUserId(userId);
            List<UUID> orderIdListOfUser = user.getOrderIdList();
            orderIdListOfUser.add(orderId);
            user.setOrderIdList(orderIdListOfUser);
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Error updating order ID list for user. Reason: " + e.getMessage());
        }
    }

    /**
     * Retrieves a CartItem from the ProductService using the product ID of the given CartItem.
     *
     * @param cartItem The CartItem containing the product ID.
     * @return The CartItem retrieved from the ProductService.
     * @throws RuntimeException if there's an error retrieving the CartItem or other unexpected issues.
     */
    private CartItem getCartItemFromProductService(CartItem cartItem) {
        try {
            return orderUtil.getCartItemFromProductService(cartItem.getProductId());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving CartItem from ProductService. Reason: " + e.getMessage());
        }
    }
}
