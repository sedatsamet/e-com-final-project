package org.sedatsamet.orderservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PlaceOrderRequest {
    private UUID userId;
    private UUID cartId;
}
