package dev.tushar.ecommerceapi.exception;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Long userId) {
        super("User with ID " + userId + " not found");
    }

    public UserNotFoundException(String email) {
        super("User with email " + email + " not found");
    }

}