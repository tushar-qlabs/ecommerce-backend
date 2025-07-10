package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.AddressRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UserUpdateRequestDTO;
import dev.tushar.ecommerceapi.dto.response.AddressResponseDTO;
import dev.tushar.ecommerceapi.dto.response.UserResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;

import java.util.List;
import java.util.Set;

public interface UserService {

    // Admin user operations
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long userId);

    // Current user operations
    UserResponseDTO getCurrentUser(CustomUserDetails currentUser);
    UserResponseDTO updateCurrentUser(CustomUserDetails currentUser, UserUpdateRequestDTO updateRequest);

    // Address operations for current user
    AddressResponseDTO addAddress(CustomUserDetails currentUser, AddressRequestDTO request);
    List<AddressResponseDTO> getAllAddresses(CustomUserDetails currentUser);
    AddressResponseDTO updateAddress(CustomUserDetails currentUser, Long addressId, AddressRequestDTO request);
    void deleteAddress(CustomUserDetails currentUser, Long addressId);
    AddressResponseDTO getAddressById(CustomUserDetails currentUser, Long addressId);
}