package org.sedatsamet.productservice.dto.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@Builder
public class UpdateProductRequest {
    private UUID productId;
    private String name;
    private String description;
    private Integer quantity;
    private MultipartFile image;
    private Double price;
}
