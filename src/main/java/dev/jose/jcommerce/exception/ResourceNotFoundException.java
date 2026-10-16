package dev.jose.jcommerce.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("Recurso não encontrado");
    }
}
