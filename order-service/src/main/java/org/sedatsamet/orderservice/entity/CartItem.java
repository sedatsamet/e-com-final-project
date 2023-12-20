package org.sedatsamet.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private UUID productId;
    private String name;
    private String description;
    private Integer quantity;
    private Double price;
    private byte[] productImage;
}
