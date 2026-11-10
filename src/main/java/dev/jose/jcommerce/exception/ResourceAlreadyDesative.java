package dev.jose.jcommerce.exception;

public class ResourceAlreadyDesative extends RuntimeException {

    public ResourceAlreadyDesative(String resourceType){
        super("Já existe um registro de " + resourceType + " com esse status");
    }
}
