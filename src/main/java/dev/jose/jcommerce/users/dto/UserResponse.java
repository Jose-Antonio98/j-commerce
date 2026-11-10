package dev.jose.jcommerce.users.dto;

import dev.jose.jcommerce.users.domain.User;
import dev.jose.jcommerce.users.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        Role role,
        Boolean ative,
        LocalDateTime createdAt,
        LocalDateTime updateAt
) {

}
