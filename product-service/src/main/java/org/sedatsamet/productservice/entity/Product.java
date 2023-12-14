package org.sedatsamet.productservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;

    private String name;
    private String description;
    private Integer quantity;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] productImage;

}
