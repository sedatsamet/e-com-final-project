package org.sedatsamet.cartservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.cartservice.dto.CartItemRequest;
import org.sedatsamet.cartservice.dto.CartResponseDto;
import org.sedatsamet.cartservice.entity.Cart;
import org.sedatsamet.cartservice.entity.CartItem;
import org.sedatsamet.cartservice.entity.User;
import org.sedatsamet.cartservice.repository.CartRepository;
import org.sedatsamet.cartservice.util.CartUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartUtil cartUtil;

    /**
     * Endpoint to add a product to the cart.
     *
     * @param request The request containing details about the product to be added.
     * @return ResponseEntity indicating the outcome of the operation.
     */
    public ResponseEntity<?> addProduct(CartItemRequest request) {
        try {
            // Check if the user is an admin
            if (cartUtil.isUserAdmin()) {
                return checkAmountOfProduct(request) ? ResponseEntity.ok(addProductToCart(request)) :
                        ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
            } else {
                // Check if the authenticated user is the same as the user associated with the request
                if (cartUtil.getAuthenticatedUser().getUserId().equals(request.getUserId())) {
                    return checkAmountOfProduct(request) ? ResponseEntity.ok(addProductToCart(request)) :
                            ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
                } else {
                    // Return unauthorized status if the user is neither an admin nor the authenticated user
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have access");
                }
            }
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Error adding product to cart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding product to cart");
        }
    }

    /**
     * Endpoint to update the amount of a cart item.
     *
     * @param request The request containing details about the cart item to be updated.
     * @return ResponseEntity indicating the outcome of the operation.
     */
    public ResponseEntity<?> updateAmountOfCartItem(CartItemRequest request) {
        try {
            // Check if the user is an admin
            if (cartUtil.isUserAdmin()) {
                return checkAmountOfProduct(request) ? ResponseEntity.ok(updateCartItem(request)) :
                        ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
            } else {
                // Check if the authenticated user is the same as the user associated with the request
                if (cartUtil.getAuthenticatedUser().getUserId().equals(request.getUserId())) {
                    return checkAmountOfProduct(request) ? ResponseEntity.ok(updateCartItem(request)) :
                            ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
                } else {
                    // Return unauthorized status if the user is neither an admin nor the authenticated user
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have access");
                }
            }
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Error updating cart item amount: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating cart item amount");
        }
    }

    /**
     * Endpoint to clear the cart for a specific user.
     *
     * @param userId The UUID of the user whose cart needs to be cleared.
     * @return ResponseEntity indicating the outcome of the operation.
     */
    public ResponseEntity<?> clearCart(UUID userId) {
        try {
            // Check if the user is an admin
            if (cartUtil.isUserAdmin()) {
                return ResponseEntity.ok(deleteAllCartItems(userId));
            } else {
                // Check if the authenticated user is the same as the user associated with the request
                if (cartUtil.getAuthenticatedUser().getUserId().equals(userId)) {
                    return ResponseEntity.ok(deleteAllCartItems(userId));
                } else {
                    // Return unauthorized status if the user is neither an admin nor the authenticated user
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have access");
                }
            }
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Error clearing the cart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error clearing the cart");
        }
    }

    /**
     * Deletes all items from the cart associated with the given user ID.
     *
     * @param userId The UUID of the user whose cart needs to be cleared.
     * @return A ResponseEntity containing the result of the operation.
     */
    private ResponseEntity<?> deleteAllCartItems(UUID userId) {
        User user = null;                       // Initialize user object
        Cart cart = null;                       // Initialize cart object
        List<CartItem> emptyList = new ArrayList<>(); // Create an empty list for cart items
        try {
            // Retrieve user details based on the provided user ID
            user = getUserFromUserService(userId);
            // Retrieve cart details based on the user's cart ID
            cart = cartRepository.findById(user.getCartId()).orElse(null);
            // Throw a runtime exception if cart or user is not found
            if (cart == null) {
                throw new RuntimeException("Cart not found while deleting all items");
            }
            if (user == null) {
                throw new RuntimeException("User not found while deleting all items");
            }
        } catch (RuntimeException e) {          // Catch specific runtime exceptions
            log.warn("Exception encountered while deleting all cart items: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {                 // Catch any other exceptions
            log.error("Error deleting all cart items: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting cart items");
        }
        // Delete the cart from the repository
        cartRepository.delete(cart);
        // Construct and return the response DTO
        return ResponseEntity.ok(CartResponseDto.builder()
                .cartId(user.getCartId())
                .userId(user.getUserId())
                .userName(user.getUsername())
                .cartItems(emptyList)
                .totalPrice(calculateTotalPrice(emptyList))
                .build());
    }

    /**
     * Retrieves the cart details for the given user ID.
     *
     * @param userId The UUID of the user whose cart details are to be fetched.
     * @return A ResponseEntity containing the cart details.
     */
    public ResponseEntity<?> getCart(UUID userId) {
        User user = null;
        List<CartItem> productListOfUser = new ArrayList<>();
        try {
            user = getUserFromUserService(userId);
            if (user != null) {
                Optional<Cart> cart = cartRepository.findById(user.getCartId());
                if (cart.isPresent()) {
                    productListOfUser = cart.get().getProducts();
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving cart details for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving cart details");
        }
        // Construct and return the response DTO
        return ResponseEntity.ok(CartResponseDto.builder()
                .cartId(user != null ? user.getCartId() : null)
                .userId(user != null ? user.getUserId() : null)
                .userName(user != null ? user.getUsername() : null)
                .cartItems(productListOfUser)
                .totalPrice(calculateTotalPrice(productListOfUser))
                .build());
    }

    /**
     * Updates the quantity of a cart item for the given user.
     *
     * @param request The CartItemRequest containing the details to update.
     * @return A CartResponseDto containing the updated cart details.
     */
    private CartResponseDto updateCartItem(CartItemRequest request) {
        User user = null;
        List<CartItem> cartItems = new ArrayList<>();
        Cart cart = null;
        CartItem cartItemWillUpdate = null;
        try {
            user = getUserFromUserService(request.getUserId());
            cart = cartRepository.findById(user.getCartId()).orElse(null);
            if (cart == null) {
                throw new RuntimeException("Cart not found");
            }
            cartItems = cart.getProducts();
            for (CartItem cartItem : cartItems) {
                if (cartItem.getProductId().equals(request.getProductId())) {
                    cartItemWillUpdate = cartItem;
                    break;
                }
            }
            if (cartItemWillUpdate == null) {
                throw new RuntimeException("Product not found");
            }
            cartItemWillUpdate.setQuantity(request.getAmount());
            cart.setTotalPrice(calculateTotalPrice(cartItems));
            cartRepository.save(cart);
        } catch (Exception e) {
            log.error("Error updating cart item: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating cart item");
        }
        // Construct and return the response DTO
        return CartResponseDto.builder()
                .cartId(user.getCartId())
                .userId(user.getUserId())
                .userName(user.getUsername())
                .cartItems(cart.getProducts())
                .totalPrice(cart.getTotalPrice())
                .build();
    }

    /**
     * Checks if the requested amount of a product can be added to the cart.
     *
     * @param request The CartItemRequest containing the product details and requested amount.
     * @return True if the amount can be added, otherwise false.
     */
    private Boolean checkAmountOfProduct(CartItemRequest request) {
        CartItem cartItem = null;
        try {
            cartItem = getCartItemFromProductService(request);
            if (cartItem.getQuantity() < request.getAmount()) {
                return false;
            }
        } catch (Exception e) {
            log.error("Error checking amount of product: {}", e.getMessage(), e);
            throw new RuntimeException("Error checking amount of product");
        }
        return true;
    }

    /**
     * Adds a product to the user's cart based on the given request.
     *
     * This method checks if the user's cart exists. If it doesn't, a new cart is created.
     * The method then checks if the product already exists in the cart. If so, the quantity is updated.
     * Otherwise, a new cart item is created and added to the cart.
     *
     * @param request The CartItemRequest containing the product details and requested amount.
     * @return A CartResponseDto representing the updated cart.
     * @throws RuntimeException If any error occurs during the process, a runtime exception is thrown.
     */
    private CartResponseDto addProductToCart(CartItemRequest request) {
        // Initialize necessary variables
        User user = null;
        Cart cart = null;
        CartItem cartItemWillAdd;
        List<CartItem> cartItems = new ArrayList<>();
        try {
            // Fetch the user details from the user service
            user = getUserFromUserService(request.getUserId());
            // Retrieve the user's cart from the repository
            cart = cartRepository.findById(user.getCartId()).orElse(null);
            // If the cart doesn't exist, create a new one
            if (cart == null) {
                cart = Cart.builder()
                        .cartId(user.getCartId())
                        .userId(user.getUserId())
                        .products(new ArrayList<>())
                        .totalPrice(0.0)
                        .build();
            }
            // Get the current list of products in the cart
            cartItems = cart.getProducts();
            CartItem existingProduct = null;
            // Check if the product already exists in the cart
            for (CartItem cartItem : cartItems) {
                if (cartItem.getProductId().equals(request.getProductId())) {
                    existingProduct = cartItem;
                    break;
                }
            }
            // Update the quantity if the product exists; otherwise, add a new cart item
            if (existingProduct != null) {
                cartItemWillAdd = existingProduct;
                cartItemWillAdd.setQuantity(cartItemWillAdd.getQuantity() + request.getAmount());
            } else {
                cartItemWillAdd = getCartItemFromProductService(request);
                cartItemWillAdd.setQuantity(request.getAmount());
                cartItems.add(cartItemWillAdd);
            }
            // Set the cart for each cart item
            for (CartItem item : cartItems) {
                item.setCart(cart);
            }
            // Update the cart's product list and total price
            cart.setProducts(cartItems);
            cart.setTotalPrice(calculateTotalPrice(cart.getProducts()));
            // Save the updated cart to the repository
            cartRepository.save(cart);
        } catch (Exception e) {
            // Log the error and throw a runtime exception with a generic error message
            log.error("Error adding product to cart: {}", e.getMessage(), e);
            throw new RuntimeException("Error adding product to cart");
        }
        // Return the updated cart details in the form of a CartResponseDto
        return CartResponseDto.builder()
                .cartId(user.getCartId())
                .userId(user.getUserId())
                .userName(user.getUsername())
                .cartItems(cart.getProducts())
                .totalPrice(cart.getTotalPrice())
                .build();
    }

    /**
     * Retrieves a CartItem from the product service based on the given request.
     *
     * @param request The CartItemRequest containing the product details and requested amount.
     * @return The CartItem obtained from the product service.
     * @throws RuntimeException If any error occurs during the process, a runtime exception is thrown.
     */
    private CartItem getCartItemFromProductService(CartItemRequest request) {
        try {
            return cartUtil.getCartItemFromProductService(request);
        } catch (Exception e) {
            // Log the error and throw a runtime exception with a generic error message
            log.error("Error fetching cart item from product service: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching cart item from product service");
        }
    }

    /**
     * Retrieves a User from the user service based on the given user ID.
     *
     * @param userId The UUID of the user to retrieve details for.
     * @return The User obtained from the user service.
     * @throws RuntimeException If any error occurs during the process, a runtime exception is thrown.
     */
    private User getUserFromUserService(UUID userId) {
        try {
            return cartUtil.getUserDetailsByUserId(userId);
        } catch (Exception e) {
            // Log the error and throw a runtime exception with a generic error message
            log.error("Error fetching user details from user service: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching user details from user service");
        }
    }

    /**
     * Calculates the total price for a list of cart items based on their quantity and price.
     *
     * @param cartItems The list of CartItems for which the total price needs to be calculated.
     * @return The total price calculated from the given cart items.
     * @throws RuntimeException If any error occurs during the calculation, a runtime exception is thrown.
     */
    private Double calculateTotalPrice(List<CartItem> cartItems) {
        Double totalPrice = 0.0;
        try {
            for (CartItem cartItem : cartItems) {
                totalPrice += cartItem.getQuantity() * cartItem.getPrice();
            }
        } catch (Exception e) {
            // Log the error and throw a runtime exception with a generic error message
            log.error("Error calculating total price: {}", e.getMessage(), e);
            throw new RuntimeException("Error calculating total price");
        }
        return totalPrice;
    }

    /**
     * Retrieves the cart details by its ID for order service.
     *
     * @param userId The ID of the user whose cart details are to be retrieved.
     * @return A Cart object representing the cart details for the given user.
     * @throws RuntimeException If any error occurs during the retrieval, a runtime exception is thrown.
     */
    public Cart getCartByCartIdForOrderService(UUID userId) {
        Optional<Cart> cart = null;
        List<CartItem> productListOfUser = null;
        User user = null;
        try {
            user = getUserFromUserService(userId);
            if (user != null) {
                cart = cartRepository.findById(user.getCartId());
                if (cart.isEmpty()) {
                    return Cart.builder()
                            .cartId(user.getCartId())
                            .userId(user.getUserId())
                            .products(new ArrayList<>())
                            .totalPrice(0.0)
                            .build();
                }
                productListOfUser = cart.get().getProducts();
            }
        } catch (Exception e) {
            // Log the error and throw a runtime exception with a generic error message
            log.error("Error retrieving cart details for order service: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving cart details for order service");
        }
        return Cart.builder()
                .cartId(user.getCartId())
                .userId(user.getUserId())
                .products(productListOfUser)
                .totalPrice(calculateTotalPrice(productListOfUser))
                .build();
    }
}
