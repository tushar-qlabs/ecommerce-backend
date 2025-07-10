package dev.tushar.ecommerceapi.exception;

public class BusinessAlreadyExistException extends ConflictException {

    public BusinessAlreadyExistException(String message) {
        super(message);
    }
}
