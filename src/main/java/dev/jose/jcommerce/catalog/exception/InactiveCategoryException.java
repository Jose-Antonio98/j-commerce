package dev.jose.jcommerce.catalog.exception;

public class InactiveCategoryException extends RuntimeException {

    public InactiveCategoryException() {
        super("A categoria escolhida está inativa.");
    }

}
