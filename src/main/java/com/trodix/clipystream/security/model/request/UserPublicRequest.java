package com.trodix.clipystream.security.model.request;

import java.util.UUID;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPublicRequest {

	@NotBlank
    private UUID id;
    
    private String username;
    
}
