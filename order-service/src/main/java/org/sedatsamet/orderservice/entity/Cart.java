package org.sedatsamet.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cart {
    private UUID cartId;
    private UUID userId;
    private List<CartItem> products;
    private Double totalPrice;
}