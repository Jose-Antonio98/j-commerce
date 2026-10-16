package dev.jose.jcommerce.users.dto;

import dev.jose.jcommerce.users.domain.User;
import dev.jose.jcommerce.users.util.EmailNormalize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UserRequest(
        @Email(message = "Informe um email valido!")
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8, max = 20)
        String password
) {
    public UserRequest {
        email = EmailNormalize.normalizeEmail(email);
    }

    public User toEntity(String encodedPassword) {
       var user = new User();
       user.setEmail(email);
       user.setPasswordHash(encodedPassword);
       return user;
    }
}