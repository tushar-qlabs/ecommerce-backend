package dev.tushar.ecommerceapi.util;

public enum ErrorCode {
    INVALID_TOKEN(100, "Invalid token."),
    INVALID_CREDENTIALS(101, "Invalid credentials."),
    USER_NOT_FOUND(102, "User not found."),
    USER_ALREADY_EXISTS(103, "User already exists."),
    PRODUCT_NOT_FOUND(104, "Product not found.");
    ErrorCode(int i, String s) {}
}
