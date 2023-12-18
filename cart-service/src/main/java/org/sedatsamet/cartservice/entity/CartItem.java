package org.sedatsamet.cartservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cart_items")
public class CartItem {

    @Id
    private UUID productId;
    private String name;
    private String description;
    private Integer quantity;
    private Double price;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] productImage;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    private Cart cart;
}
