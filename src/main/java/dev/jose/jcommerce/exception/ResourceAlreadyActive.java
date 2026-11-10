package dev.jose.jcommerce.exception;

public class ResourceAlreadyActive extends RuntimeException {

    public ResourceAlreadyActive(String resourceType){
        super("Já existe um registro de " + resourceType + " com esse status");
    }
}
