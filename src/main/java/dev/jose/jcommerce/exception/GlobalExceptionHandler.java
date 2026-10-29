package dev.jose.jcommerce.exception;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Trata erros de validação dos campos da requisição.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        List<String> messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();

        return response(HttpStatus.BAD_REQUEST, "Erro de validação", messages);
    }

    // Trata parâmetros recebidos com tipo ou formato inválido.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameterType(MethodArgumentTypeMismatchException ex) {
        String parameter = ex.getName();
        return response(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                List.of("Parâmetro '" + parameter + "' inválido."));
    }

    // Trata parâmetros obrigatórios ausentes na requisição.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException ex) {
        return response(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente",
                List.of("O parâmetro '" + ex.getParameterName() + "' é obrigatório."));
    }

    // Trata cabeçalhos obrigatórios ausentes na requisição.
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        return response(HttpStatus.BAD_REQUEST, "Cabeçalho obrigatório ausente",
                List.of("O cabeçalho '" + ex.getHeaderName() + "' é obrigatório."));
    }

    // Trata conflitos quando um recurso já está cadastrado.
    @ExceptionHandler(ResourceAlreadyRegistered.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyRegistered(ResourceAlreadyRegistered ex) {
        return response(HttpStatus.CONFLICT, "Conflito", List.of(ex.getMessage()));
    }

    // Trata exceções inesperadas não cobertas por handlers específicos.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex, HttpServletRequest request) {

        log.error("Erro interno | método={} | URI={}", request.getMethod(), request.getRequestURI(), ex);

        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor",
                List.of("Ocorreu um erro inesperado.")
        );
    }

    // Trata corpos de requisição inválidos ou JSON malformado.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return response(HttpStatus.BAD_REQUEST, "Requisição inválida",
                List.of("O corpo da requisição contém JSON inválido ou malformado."));
    }

    // Trata violações de constraints de validação.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> messages = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return response(HttpStatus.BAD_REQUEST, "Erro de validação", messages);
    }

    // Trata itens não encontrados no banco de dados
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "Resource", List.of(ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationError(AuthenticationException ex) {
        return response(HttpStatus.UNAUTHORIZED, "Acesso negado", List.of("Email ou senha inválidos"));
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status,
            String titulo,
            List<String> mensagens) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                titulo,
                mensagens
        );

        return ResponseEntity.status(status).body(response);
    }
}
