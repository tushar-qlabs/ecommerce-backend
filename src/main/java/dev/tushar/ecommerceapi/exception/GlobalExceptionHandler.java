package dev.tushar.ecommerceapi.exception;


import dev.tushar.ecommerceapi.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle UserNotFoundException: If the user is not found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error(e.getMessage(), null, HttpStatus.NOT_FOUND.value())
        );
    }

    // Handle EmailOrPhoneAlreadyExistsException: If the email or phone already exists
    @ExceptionHandler(EmailOrPhoneAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleEmailOrPhoneAlreadyExists(EmailOrPhoneAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ApiResponse.error(e.getTitle(), e.getMessage(), HttpStatus.ACCEPTED.value())
        );
    }

    @ExceptionHandler(BusinessAlreadyExistException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessAlreadyExist(BusinessAlreadyExistException e) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ApiResponse.error("Registration Failed", e.getMessage(), HttpStatus.ACCEPTED.value())
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error(e.getMessage(), "Invalid email or password", HttpStatus.UNAUTHORIZED.value())
        );
    }

    // Handle MethodArgumentNotValidException: If the request body is not valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid value"),
                        (existing, replacement) -> existing,
                        HashMap::new
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Validation Error", errors, HttpStatus.BAD_REQUEST.value())
        );
    }

    // -- Generic Exception Handler -- //
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<?>> handleConflictException(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.error(e.getMessage(), null, HttpStatus.CONFLICT.value())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Internal Server Error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
    }
}
