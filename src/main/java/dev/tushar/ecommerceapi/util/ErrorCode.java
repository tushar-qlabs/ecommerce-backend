package dev.tushar.ecommerceapi.util;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // --- Authentication & Authorization (100-199) ---
    ACCOUNT_ALREADY_EXISTS(100, "An account with the provided email already exists."),
    USER_NOT_FOUND(101, "A user with the provided ID/email could not be found."),
    ROLE_NOT_FOUND(102, "A role with the provided ID could not be found."),
    PERMISSION_NOT_FOUND(103, "A permission with the provided ID could not be found."),
    MISSING_CUSTOMER_ROLE(104, "Server configuration error: The 'CUSTOMER' role is missing."),
    MISSING_SELLER_ROLE(105, "Server configuration error: The 'SELLER' role is missing."),
    MISSING_CREATE_BUSINESS_PERMISSION(106, "Server configuration error: The 'CREATE_BUSINESS' permission is missing."),
    INVALID_CREDENTIALS(107, "Invalid credentials provided."),
    ACCESS_DENIED(108, "You do not have permission to access this resource."),

    // --- Business & Seller (200-299) ---
    BUSINESS_ALREADY_REGISTERED(200, "This account already has a registered business."),
    BUSINESS_NOT_FOUND(201, "No business has been registered for this account or with the given ID."),
    BUSINESS_NOT_VERIFIED(202, "Your business is not verified yet."),
    INVALID_BUSINESS_STATUS(203, "The provided status value is invalid."),
    BUSINESS_MISSING_USER(204, "Data integrity error: Business has no associated user."),
    MUST_BE_BUSINESS_OWNER(205, "You must have a registered business to perform this action."),

    // --- Product, Category, & Catalog (300-399) ---
    CATEGORY_NOT_FOUND(300, "Category with the provided ID not found."),
    PARENT_CATEGORY_NOT_FOUND(301, "Parent category with the provided ID not found."),
    ATTRIBUTE_NOT_FOUND(302, "Attribute with the provided ID not found."),
    OPTION_SET_NOT_FOUND(303, "OptionSet with the provided ID not found."),
    OPTION_SET_ID_REQUIRED(304, "optionSetId is required for ENUM attribute type."),
    CATEGORY_HAS_PRODUCTS(305, "Cannot delete. The category '{categoryName}' (or one of its sub-categories) has products assigned to it."),
    PRODUCT_NOT_FOUND(306, "Product with the provided ID not found."),
    PRODUCT_VARIANT_NOT_FOUND(307, "Product variant not found."),
    INVALID_PRODUCT_ATTRIBUTES(308, "Product attributes do not match the attributes required by the category. Required: {requiredAttributes}, but you provided {providedAttributes}."),
    INVALID_COLOR_HEX(309, "Invalid hex color code for attribute 'Color'. It must be in #RRGGBB format."),
    INVALID_ENUM_VALUE(310, "Invalid value for attribute '{attributeName}'. Allowed values are: {allowedValues}."),
    INVALID_BOOLEAN_VALUE(311, "Value for '{attributeName}' must be a boolean (true/false)."),
    PRIMARY_VARIANT_MISSING(312, "Data consistency error: Product has no primary variant set."),

    // --- Cart, Order, & Wishlist (400-499) ---
    CART_NOT_FOUND(400, "Cart not found."),
    EMPTY_CART_ORDER(401, "Cannot create an order from an empty cart."),
    PAYMENT_FAILED(402, "Payment failed."),
    ADDRESS_NOT_FOUND(403, "The requested address could not be found."),
    SHIPPING_ADDRESS_NOT_FOUND(404, "Shipping address not found."),
    INSUFFICIENT_STOCK(405, "Insufficient stock for product: {productName}. Only {stockQuantity} left in stock."),
    ORDER_NOT_FOUND(406, "Order not found."),
    CANNOT_DELETE_DEFAULT_ADDRESS(407, "Cannot delete the default address. Please set another address as default first."),

    // --- General & Validation (500-599) ---
    VALIDATION_FAILED(500, "Validation failed."),
    UNEXPECTED_ERROR(501, "An unexpected error occurred.");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}