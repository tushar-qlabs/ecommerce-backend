package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.AddressRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UserUpdateRequestDTO;
import dev.tushar.ecommerceapi.dto.response.AddressResponseDTO;
import dev.tushar.ecommerceapi.dto.response.UserResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_ALL_USERS')")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "All users fetched successfully.",
                        users,
                        HttpStatus.OK.value())
        );
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('READ_ALL_USERS')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long userId) {
        UserResponseDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User fetched successfully.",
                        user,
                        HttpStatus.OK.value())
        );
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        UserResponseDTO userResponse = userService.getCurrentUser(currentUser);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User profile fetched successfully.",
                        userResponse,
                        HttpStatus.OK.value())
        );
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('UPDATE_MY_PROFILE')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateCurrentUser(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UserUpdateRequestDTO updateRequest) {
        UserResponseDTO updatedUser = userService.updateCurrentUser(currentUser, updateRequest);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User profile updated successfully.",
                        updatedUser,
                        HttpStatus.OK.value())
        );
    }

    @PostMapping("/me/addresses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AddressResponseDTO>> addAddress(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO newAddress = userService.addAddress(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Address added successfully.",
                        newAddress,
                        HttpStatus.CREATED.value())
        );
    }

    @GetMapping("/me/addresses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AddressResponseDTO>>> getAllAddresses(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<AddressResponseDTO> addresses = userService.getAllAddresses(currentUser);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User addresses fetched successfully.",
                        addresses,
                        HttpStatus.OK.value())
        );
    }

    @GetMapping("/me/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AddressResponseDTO>> getAddressById(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long addressId) {
        AddressResponseDTO address = userService.getAddressById(currentUser, addressId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Address fetched successfully.",
                        address,
                        HttpStatus.OK.value())
        );
    }

    @PutMapping("/me/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AddressResponseDTO>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO updatedAddress = userService.updateAddress(currentUser, addressId, request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Address updated successfully.",
                        updatedAddress,
                        HttpStatus.OK.value())
        );
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long addressId) {
        userService.deleteAddress(currentUser, addressId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Address deleted successfully.",
                        null,
                        HttpStatus.OK.value())
        );
    }
}
