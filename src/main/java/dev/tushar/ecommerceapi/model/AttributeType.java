package dev.tushar.ecommerceapi.model;

// * Defines the data type of product attribute for clothing products.

// * - TEXT: Free-form descriptive text (e.g., "Slim Fit", "Cotton-Polyester Blend").
// * - NUMBER: A numerical value (e.g., "Length: 42", "Chest: 38.5").
// * - BOOLEAN: A true/false flag (e.g., "Stretchable": false).

public enum AttributeType {
    TEXT,
    NUMBER,
    BOOLEAN,
    ENUM
}
