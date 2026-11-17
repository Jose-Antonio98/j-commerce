package dev.jose.jcommerce.users.controller;

import dev.jose.jcommerce.auth.dto.LoginRequest;
import dev.jose.jcommerce.auth.dto.LoginResponse;
import dev.jose.jcommerce.auth.service.AuthService;
import dev.jose.jcommerce.exception.ErrorResponse;
import dev.jose.jcommerce.users.dto.UserRequest;
import dev.jose.jcommerce.users.dto.UserResponse;
import dev.jose.jcommerce.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação e usuários", description = "Cadastro, autenticação e consulta administrativa de usuários.")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @Operation(summary = "Cadastra um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@RequestBody @Valid UserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @Operation(summary = "Autentica um usuário e retorna um token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação realizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest loginRequest) {
        String token = authService.login(loginRequest);
        return new LoginResponse(token, "Bearer");
    }

    @Operation(summary = "Busca um usuário pelo ID", description = "Requer a role ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/admin/users/{id}")
    public UserResponse findUserById(
            @Parameter(description = "ID do usuário", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id
    ) {
        return userService.findUserById(id);
    }
}
