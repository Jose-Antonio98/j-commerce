package dev.jose.jcommerce.users.controller;

import dev.jose.jcommerce.auth.dto.LoginRequest;
import dev.jose.jcommerce.auth.dto.LoginResponse;
import dev.jose.jcommerce.auth.service.AuthService;
import dev.jose.jcommerce.users.dto.UserRequest;
import dev.jose.jcommerce.users.dto.UserResponse;
import dev.jose.jcommerce.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@RequestBody @Valid UserRequest userRequest) {
        userService.createUser(userRequest);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest loginRequest) {
        String token = authService.login(loginRequest);
        return new LoginResponse(token, "Bearer");
    }

    @GetMapping("/admin/users/{id}")
    public UserResponse findUserById(@PathVariable UUID id) {
        return userService.findUserById(id);
    }
}
