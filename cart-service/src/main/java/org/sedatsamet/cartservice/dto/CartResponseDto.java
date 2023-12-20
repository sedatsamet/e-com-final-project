package org.sedatsamet.cartservice.dto;

import lombok.Builder;
import lombok.Data;
import org.sedatsamet.cartservice.entity.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartResponseDto {
    private UUID cartId;
    private UUID userId;
    private String userName;
    private List<CartItem> cartItems = new ArrayList<>();
    private Double totalPrice;
}
