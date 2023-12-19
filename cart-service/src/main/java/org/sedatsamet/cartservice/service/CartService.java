package org.sedatsamet.cartservice.service;

import org.sedatsamet.cartservice.dto.CartItemRequest;
import org.sedatsamet.cartservice.dto.CartResponseDto;
import org.sedatsamet.cartservice.entity.Cart;
import org.sedatsamet.cartservice.entity.CartItem;
import org.sedatsamet.cartservice.entity.User;
import org.sedatsamet.cartservice.repository.CartRepository;
import org.sedatsamet.cartservice.util.CartUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    public ResponseEntity<CartResponseDto> addProduct(CartItemRequest request) {
        return checkAmountOfProduct(request) ? ResponseEntity.ok(addProductToCart(request)) : ResponseEntity.badRequest().build();
    }

    public ResponseEntity<CartResponseDto> updateAmountOfCartItem(CartItemRequest request) {
        return checkAmountOfProduct(request) ? ResponseEntity.ok(updateCartItem(request)) : ResponseEntity.badRequest().build();
    }

    public CartResponseDto clearCart(UUID userId) {
        User user = getUserFromUserService(userId);
        Cart cart = cartRepository.findById(user.getCartId()).orElse(null);
        List<CartItem> emptyList = new ArrayList<>();
        if (cart == null) {
            throw new RuntimeException("Cart not found");
        } else {
            cartRepository.delete(cart);
        }
        return CartResponseDto.builder().cartId(user.getCartId()).userId(user.getUserId()).userName(user.getName()).cartItems(emptyList).build();
    }

    public CartResponseDto getCart(UUID userId) {
        Optional<Cart> cart = null;
        List<CartItem> productListOfUser = null;
        User user = getUserFromUserService(userId);
        if (user != null) {
            cart = cartRepository.findById(user.getCartId());
            if (cart.isEmpty()) {
                return CartResponseDto.builder().cartId(user.getCartId()).userId(user.getUserId()).userName(user.getName()).cartItems(new ArrayList<>()).build();
            }
            productListOfUser = cart.get().getProducts();
        }
        return CartResponseDto.builder().cartId(user.getCartId()).userId(user.getUserId()).userName(user.getName()).cartItems(productListOfUser).build();
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
        cartRepository.save(cart);
        return CartResponseDto.builder().cartId(user.getCartId()).userId(user.getUserId()).userName(user.getName()).cartItems(cart.getProducts()).build();
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
            cart = Cart.builder().cartId(user.getCartId()).userId(user.getUserId()).products(new ArrayList<>()).build();
            cartItemWillAdd = getCartItemFromProductService(request);
            cartItemWillAdd.setQuantity(request.getAmount());
            cart.getProducts().add(cartItemWillAdd);
            cartItemWillAdd.setCart(cart);
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
                cartItemWillAdd = getCartItemFromProductService(request);
                cartItemWillAdd.setQuantity(request.getAmount());
                cart.getProducts().add(cartItemWillAdd);
            }

            if (existingProduct != null) {
                cartItemWillAdd = existingProduct;
                cartItemWillAdd.setQuantity(cartItemWillAdd.getQuantity() + request.getAmount());
            } else {
                cartItems.add(getCartItemFromProductService(request));
            }
        }
        cartRepository.save(cart);
        return CartResponseDto.builder().cartId(user.getCartId()).userId(user.getUserId()).userName(user.getName()).cartItems(cart.getProducts()).build();
    }

    private CartItem getCartItemFromProductService(CartItemRequest request) {
        return cartUtil.getCartItemFromProductService(request);
    }

    private User getUserFromUserService(UUID userId) {
        return cartUtil.getUserDetailsByUserId(userId);
    }
}