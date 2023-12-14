package org.sedatsamet.productservice.dto.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Data
public class CreateProductRequest {

    private String name;
    private String description;
    private Integer quantity;
    private MultipartFile image;
}
