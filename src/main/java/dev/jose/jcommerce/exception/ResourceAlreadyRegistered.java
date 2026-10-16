package dev.jose.jcommerce.exception;

public class ResourceAlreadyRegistered extends RuntimeException{

    public ResourceAlreadyRegistered(String resourceType){
        super("Já existe um registro de " + resourceType + " com esses dados");
    }

}
