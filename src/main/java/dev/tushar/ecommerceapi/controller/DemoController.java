package dev.tushar.ecommerceapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    // Public endpoint, remains the same.
    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is a public endpoint.");
    }

    // Generic endpoint for any authenticated user.
    @GetMapping("/secure-resource")
    public ResponseEntity<String> authenticatedEndpoint() {
        return ResponseEntity.ok("Hello from a secured endpoint! Only authenticated users can see this.");
    }

    // Generic endpoint for users with the 'WRITE_PRODUCTS' permission.
    @GetMapping("/action-resource")
    @PreAuthorize("hasAuthority('WRITE_PRODUCTS')")
    public ResponseEntity<String> sellerEndpoint() {
        return ResponseEntity.ok("Hello from an endpoint requiring the 'WRITE_PRODUCTS' permission!");
    }

    // Admin endpoint, remains the same.
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("Hello from an admin-only endpoint!");
    }
}