package dev.tushar.ecommerceapi.model;

import lombok.Getter;

@Getter
public enum PermissionKey {
    // --- User Profile Permissions ---
    UPDATE_MY_PROFILE("Allows a user to update their own profile information."),

    // --- Business Management Permissions (for Sellers) ---
    CREATE_BUSINESS("Allows a user to register as a business account."),

    // --- Product Catalog Permissions ---
    READ_PRODUCTS("Allows any user to browse and view public product listings."),
    CREATE_PRODUCTS("Allows a seller to create new products for their business."),
    UPDATE_PRODUCTS("Allows a seller to update their own product details."),
    DELETE_PRODUCTS("Allows a seller to soft-delete their own products."),

    // --- Order Management Permissions ---
    CREATE_ORDERS("Allows a customer to create a new order during checkout."),
    READ_MY_ORDERS("Allows a customer to view their own order history."),
    READ_SELLER_ORDERS("Allows a seller to view orders placed for their products."),
    UPDATE_SELLER_ORDERS("Allows a seller to update the status of their orders."),

    // --- Administrative Permissions ---
    READ_ALL_USERS("Allows an admin to retrieve a list of all users in the system."),
    MANAGE_ROLES("Allows an admin to create, delete, and manage roles and permissions."),
    READ_ALL_BUSINESSES("Allows an admin to retrieve a list of all businesses."); // <-- New Permission

    private final String description;

    PermissionKey(String description) {
        this.description = description;
    }
}
