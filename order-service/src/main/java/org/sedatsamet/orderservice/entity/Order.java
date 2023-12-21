package org.sedatsamet.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    @Id
    private UUID orderId;
    private UUID userId;
    private Double totalPrice;
    private Double totalAmountOfProduct;
    private Boolean transactionApproved;

    @ElementCollection(targetClass = UUID.class, fetch = FetchType.EAGER)
    private List<UUID> productIdList;
}