package dev.jose.jcommerce.users.controller;

import dev.jose.jcommerce.users.dto.UserRequest;
import dev.jose.jcommerce.users.dto.UserResponse;
import dev.jose.jcommerce.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@RequestBody @Valid UserRequest userRequest) {
        userService.createUser(userRequest);
    }

    @GetMapping("/users/{id}")
    public UserResponse findUserById(@PathVariable UUID id) {
        return userService.findUserById(id);
    }
}
