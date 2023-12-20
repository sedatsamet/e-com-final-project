package org.sedatsamet.cartservice.service;

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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartUtil cartUtil;

    public ResponseEntity<?> addProduct(CartItemRequest request) {
        if(cartUtil.isUserAdmin()){
            return checkAmountOfProduct(request) ? ResponseEntity.ok(addProductToCart(request)) :
                    ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
        }else {
            if(cartUtil.getAuthenticatedUser().getUserId().equals(request.getUserId())){
                return checkAmountOfProduct(request) ? ResponseEntity.ok(addProductToCart(request)) :
                        ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
            }else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have an acess");
            }
        }
    }

    public ResponseEntity<?> updateAmountOfCartItem(CartItemRequest request) {
        if(cartUtil.isUserAdmin()){
            return checkAmountOfProduct(request) ? ResponseEntity.ok(updateCartItem(request)) :
                    ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
        }else {
            if(cartUtil.getAuthenticatedUser().getUserId().equals(request.getUserId())){
                return checkAmountOfProduct(request) ? ResponseEntity.ok(updateCartItem(request)) :
                        ResponseEntity.status(HttpStatus.CONFLICT).body("Product out of stock");
            }else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have an acess");
            }
        }
    }

    public ResponseEntity<?> clearCart(UUID userId) {
        if(cartUtil.isUserAdmin()){
            return ResponseEntity.ok(deleteAllCartItems(userId));
        }else {
            if(cartUtil.getAuthenticatedUser().getUserId().equals(userId)){
                return ResponseEntity.ok(deleteAllCartItems(userId));
            }else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have an acess");
            }
        }
    }

    private ResponseEntity<?> deleteAllCartItems(UUID userId) {
        User user = null;
        Cart cart = null;
        List<CartItem> emptyList = new ArrayList<>();
        try {
            user = getUserFromUserService(userId);
            cart = cartRepository.findById(user.getCartId()).orElse(null);
            if(cart == null) {
                throw new ClassNotFoundException("Cart not found while deleting all items");
            }
            if(user == null) {
                throw new UsernameNotFoundException("User not found while deleting all items");
            }
        }catch (ClassNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart not found while deleting all items");
        }catch (UsernameNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found while deleting all items");
        }
        cartRepository.delete(cart);
        return ResponseEntity.ok(CartResponseDto.builder()
                .cartId(user.getCartId())
                .userId(user.getUserId())
                .userName(user.getUsername())
                .cartItems(emptyList)
                .totalPrice(calculateTotalPrice(emptyList))
                .build());
    }

    public ResponseEntity<?> getCart(UUID userId) {
        Optional<Cart> cart = null;
        List<CartItem> productListOfUser = null;
        User user = getUserFromUserService(userId);
        if (user != null) {
            cart = cartRepository.findById(user.getCartId());
            if (cart.isEmpty()) {
                return ResponseEntity.ok(CartResponseDto.builder()
                        .cartId(user.getCartId())
                        .userId(user.getUserId())
                        .totalPrice(0.0)
                        .userName(user.getUsername())
                        .cartItems(new ArrayList<>())
                        .totalPrice(calculateTotalPrice(List.of()))
                        .build());
            }
            productListOfUser = cart.get().getProducts();
        }
        return ResponseEntity.ok(CartResponseDto.builder()
                .cartId(user.getCartId())
                .userId(user.getUserId())
                .userName(user.getUsername())
                .cartItems(productListOfUser)
                .totalPrice(calculateTotalPrice(productListOfUser))
                .build());
    }

    private CartResponseDto updateCartItem(CartItemRequest request) {
        User user = getUserFromUserService(request.getUserId());
        Cart cart = cartRepository.findById(user.getCartId()).orElse(null);
        CartItem cartItemWillUpdate;
        List<CartItem> cartItems = new ArrayList<>();
        if (cart == null) {
            throw new RuntimeException("Cart not found");
        } else {
            cartItems = cart.getProducts();
            CartItem existingProduct = null;
            try {
                for (CartItem cartItem : cartItems) {
                    if (cartItem.getProductId().equals(request.getProductId())) {
                        existingProduct = cartItem;
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Product not found");
            }
            if (existingProduct != null) {
                cartItemWillUpdate = existingProduct;
                cartItemWillUpdate.setQuantity(request.getAmount());
            } else {
                throw new RuntimeException("Product not found");
            }
        }
        cart.setTotalPrice(calculateTotalPrice(cartItems));
        cartRepository.save(cart);
        return CartResponseDto.builder()
                .cartId(user.getCartId())
                .userId(user.getUserId())
                .userName(user.getUsername())
                .cartItems(cart.getProducts())
                .build();
    }

    private Boolean checkAmountOfProduct(CartItemRequest request) {
        CartItem cartItem = getCartItemFromProductService(request);
        if (cartItem.getQuantity() < request.getAmount()) {
            return false;
        }
        return true;
    }

    private CartResponseDto addProductToCart(CartItemRequest request) {
        User user = getUserFromUserService(request.getUserId());
        Cart cart = cartRepository.findById(user.getCartId()).orElse(null);
        CartItem cartItemWillAdd;
        List<CartItem> cartItems = new ArrayList<>();
        if (cart == null) {
            cart = Cart.builder()
                    .cartId(user.getCartId())
                    .userId(user.getUserId())
                    .products(new ArrayList<>())
                    .totalPrice(0.0) // Eklendi
                    .build();
            cartItemWillAdd = getCartItemFromProductService(request);
            cartItemWillAdd.setQuantity(request.getAmount());
            cart.getProducts().add(cartItemWillAdd);
            cartItemWillAdd.setCart(cart);
            cart.setTotalPrice(calculateTotalPrice(cart.getProducts()));
        } else {
            cartItems = cart.getProducts();
            System.out.println(cartItems.size());
            CartItem existingProduct = null;
            try {
                for (CartItem cartItem : cartItems) {
                    if (cartItem.getProductId().equals(request.getProductId())) {
                        existingProduct = cartItem;
                        break;
                    }
                }
            } catch (Exception e) {
                cartItemWillAdd = getCartItemFromProductService(request);
                cartItemWillAdd.setQuantity(request.getAmount());
                cart.getProducts().add(cartItemWillAdd);
                cartItemWillAdd.setCart(cart);
                cart.setTotalPrice(calculateTotalPrice(cart.getProducts()));
            }
            if (existingProduct != null) {
                cartItemWillAdd = existingProduct;
                cartItemWillAdd.setQuantity(cartItemWillAdd.getQuantity() + request.getAmount());
                cartItemWillAdd.setCart(cart);
                cart.setTotalPrice(calculateTotalPrice(cart.getProducts()));
            } else {
                cartItemWillAdd = getCartItemFromProductService(request);
                cartItemWillAdd.setQuantity(request.getAmount());
                cartItems.add(cartItemWillAdd);
                for(CartItem item: cartItems) {
                    item.setCart(cart);
                }
                cart.setProducts(cartItems);
                cart.setTotalPrice(calculateTotalPrice(cart.getProducts()));
            }
        }
        cartRepository.save(cart);
        return CartResponseDto.builder()
                .cartId(user.getCartId())
                .userId(user.getUserId())
                .userName(user.getUsername())
                .cartItems(cart.getProducts())
                .totalPrice(cart.getTotalPrice())
                .build();
    }

    private CartItem getCartItemFromProductService(CartItemRequest request) {
        return cartUtil.getCartItemFromProductService(request);
    }

    private User getUserFromUserService(UUID userId) {
        return cartUtil.getUserDetailsByUserId(userId);
    }

    private Double calculateTotalPrice(List<CartItem> cartItems) {
        Double totalPrice = 0.0;
        for (CartItem cartItem : cartItems) {
            totalPrice += cartItem.getQuantity() * cartItem.getPrice();
        }
        return totalPrice;
    }

    public Cart getCartByCartIdForOrderService(UUID userId) {
        Optional<Cart> cart = null;
        List<CartItem> productListOfUser = null;
        User user = getUserFromUserService(userId);
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
        return Cart.builder()
                .cartId(user.getCartId())
                .userId(user.getUserId())
                .products(productListOfUser)
                .totalPrice(calculateTotalPrice(productListOfUser))
                .build();
    }
}
