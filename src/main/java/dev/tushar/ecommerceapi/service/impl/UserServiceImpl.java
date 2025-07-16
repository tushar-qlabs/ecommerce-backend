package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.AddressRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UserUpdateRequestDTO;
import dev.tushar.ecommerceapi.dto.response.AddressResponseDTO;
import dev.tushar.ecommerceapi.dto.response.UserResponseDTO;
import dev.tushar.ecommerceapi.entity.Address;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.repository.AddressRepository;
import dev.tushar.ecommerceapi.repository.UserRepository;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponseDTO)
                .toList();
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "A user with the provided ID could not be found."
                ));
        return new UserResponseDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhoneNumber());
    }

    @Override
    public UserResponseDTO getCurrentUser(CustomUserDetails currentUser) {
        return mapToUserResponseDTO(currentUser.user());
    }

    @Override
    public UserResponseDTO updateCurrentUser(CustomUserDetails currentUser, UserUpdateRequestDTO updateRequest) {
        User user = currentUser.user();
        if (updateRequest.getFirstName() != null) user.setFirstName(updateRequest.getFirstName());
        if (updateRequest.getLastName() != null) user.setLastName(updateRequest.getLastName());
        if (updateRequest.getPhoneNumber() != null) user.setPhoneNumber(updateRequest.getPhoneNumber());
        return mapToUserResponseDTO(userRepository.save(user));
    }

    @Override
    public AddressResponseDTO addAddress(CustomUserDetails currentUser, AddressRequestDTO request) {
        return saveAddress(currentUser, null, request);
    }

    @Override
    public AddressResponseDTO updateAddress(CustomUserDetails currentUser, Long addressId, AddressRequestDTO request) {
        return saveAddress(currentUser, addressId, request);
    }

    @Override
    public List<AddressResponseDTO> getAllAddresses(CustomUserDetails currentUser) {
        return addressRepository.findAllByUser(currentUser.user())
                .stream()
                .map(this::mapToAddressResponseDTO)
                .toList();
    }

    @Override
    public AddressResponseDTO getAddressById(CustomUserDetails currentUser, Long addressId) {
        Address address = getOwnedAddress(currentUser, addressId);
        return mapToAddressResponseDTO(address);
    }

    @Override
    public void deleteAddress(CustomUserDetails currentUser, Long addressId) {
        Address addressToDelete = getOwnedAddress(currentUser, addressId);
        if (addressToDelete.isDefault()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot delete the default address. Please set another address as default first.");
        }
        addressRepository.delete(addressToDelete);
    }

    public AddressResponseDTO saveAddress(CustomUserDetails currentUser, Long addressId, AddressRequestDTO request) {
        User user = currentUser.user();
        Address address;

        if (addressId == null) { // We are creating a new address
            address = new Address();
            address.setUser(user);
        } else { // We are updating an existing address
            address = getOwnedAddress(currentUser, addressId);
        }

        applyRequestToAddress(address, request, user);

        Address savedAddress = addressRepository.save(address);
        return mapToAddressResponseDTO(savedAddress);
    }

    private void applyRequestToAddress(Address address, AddressRequestDTO request, User user) {

        // Check if the request made to update/add the address already have
        // the isDefault field set to true. If so, it means we need to unset
        // other existing default address to false.

        if (Boolean.TRUE.equals(request.isDefault()) && !address.isDefault()) {
            unsetCurrentDefaultAddress(user);
        }

        if (request.isDefault() != null) {
            address.setDefault(request.isDefault());
        }

        address.setLabel(request.label());
        address.setStreetLine1(request.streetLine1());
        address.setStreetLine2(request.streetLine2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountryCode(request.countryCode());
    }

    private Address getOwnedAddress(CustomUserDetails currentUser, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "The requested address could not be found."
                ));
        if (!address.getUser().getId().equals(currentUser.user().getId())) {
            throw new AccessDeniedException("You do not have permission to access this address.");
        }
        return address;
    }


    // We will get all the addresses of the user and filter it to find the
    // address that is currently set as default.
    // After that, we will get the first address from the filtered list.
    // It returning optional, so we will check if present then and pass the
    // Consumer function to make it set its default to false and save it.
    private void unsetCurrentDefaultAddress(User user) {
        addressRepository.findAllByUser(user).stream()
                .filter(Address::isDefault)
                .findFirst()
                .ifPresent(oldDefault -> {
                    oldDefault.setDefault(false);
                    addressRepository.save(oldDefault);
                });
    }

    private AddressResponseDTO mapToAddressResponseDTO(Address address) {
        return new AddressResponseDTO(
                address.getId(),
                address.getLabel(),
                address.getStreetLine1(),
                address.getStreetLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountryCode(),
                address.isDefault()
        );
    }

    public UserResponseDTO mapToUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }
}
