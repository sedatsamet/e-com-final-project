package org.sedatsamet.cartservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CartItemRequest {
    private UUID productId;
    private UUID userId;
    private Double amount;
}
