package org.sedatsamet.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreatedResponse {

    private String userId;
}
