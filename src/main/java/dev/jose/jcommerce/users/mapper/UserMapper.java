package dev.jose.jcommerce.users.mapper;

import dev.jose.jcommerce.users.domain.User;
import dev.jose.jcommerce.users.dto.UserRequest;
import dev.jose.jcommerce.users.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequest userRequest, String encodedPassword) {
        var user = new User();
        user.setEmail(userRequest.email());
        user.setPasswordHash(encodedPassword);
        return user;
    }

    public UserResponse toResponse(User user) {
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
