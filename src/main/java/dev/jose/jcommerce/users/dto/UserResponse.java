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
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
