package dev.jose.jcommerce.auth.dto;

import java.io.Serializable;

public record LoginResponse(String accessToken, String tokenType) {

}
