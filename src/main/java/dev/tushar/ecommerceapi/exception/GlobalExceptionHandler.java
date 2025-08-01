package dev.tushar.ecommerceapi.exception;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<?>> handleApiException(ApiException e) {
        return buildErrorResponse(e.getStatus(), e.getMessage(), e.getErrorDetails());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "The requested endpoint does not exist.", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException e) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), "You do not have permission to access this resource.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value"
                ));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleOtherExceptions(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", ex.getMessage());
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(HttpStatus status, String message, Object errorDetails) {
        ApiResponse<?> apiResponse = ApiResponse.error(message, errorDetails, status.value());
        return new ResponseEntity<>(apiResponse, status);
    }
}