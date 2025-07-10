package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.AddressRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UserUpdateRequestDTO;
import dev.tushar.ecommerceapi.dto.response.AddressResponseDTO;
import dev.tushar.ecommerceapi.dto.response.UserResponseDTO;
import dev.tushar.ecommerceapi.entity.Address;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.exception.ResourceNotFoundException;
import dev.tushar.ecommerceapi.exception.UserNotFoundException;
import dev.tushar.ecommerceapi.repository.AddressRepository;
import dev.tushar.ecommerceapi.repository.UserRepository;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
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
                .map(user -> new UserResponseDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhoneNumber()))
                .toList();
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return new UserResponseDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhoneNumber());
    }

    @Override
    public UserResponseDTO getCurrentUser(CustomUserDetails currentUser) {
        User user = currentUser.user();
        return new UserResponseDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhoneNumber());
    }

    @Override
    public UserResponseDTO updateCurrentUser(CustomUserDetails currentUser, UserUpdateRequestDTO updateRequest) {
        User user = currentUser.user();
        if (updateRequest.getFirstName() != null) user.setFirstName(updateRequest.getFirstName());
        if (updateRequest.getLastName() != null) user.setLastName(updateRequest.getLastName());
        if (updateRequest.getPhoneNumber() != null) user.setPhoneNumber(updateRequest.getPhoneNumber());
        user = userRepository.save(user);
        return new UserResponseDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhoneNumber());
    }

    @Override
    public AddressResponseDTO addAddress(CustomUserDetails currentUser, AddressRequestDTO request) {
        Address address = Address.builder()
                .label(request.label())
                .streetLine1(request.streetLine1())
                .streetLine2(request.streetLine2())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .countryCode(request.countryCode())
                .user(currentUser.user())
                .build();
        address = addressRepository.save(address);
        return new AddressResponseDTO(address.getId(), address.getLabel(), address.getStreetLine1(),
                address.getStreetLine2(), address.getCity(), address.getState(), address.getPostalCode(), address.getCountryCode());
    }

    @Override
    public List<AddressResponseDTO> getAllAddresses(CustomUserDetails currentUser) {
        return addressRepository.findAllByUser(currentUser.user())
                .stream()
                .map(address -> new AddressResponseDTO(address.getId(), address.getLabel(), address.getStreetLine1(),
                        address.getStreetLine2(), address.getCity(), address.getState(), address.getPostalCode(), address.getCountryCode()))
                .toList();
    }

    @Override
    public AddressResponseDTO getAddressById(CustomUserDetails currentUser, Long addressId) {
        Address address = getOwnedAddress(currentUser, addressId);
        return new AddressResponseDTO(address.getId(), address.getLabel(), address.getStreetLine1(),
                address.getStreetLine2(), address.getCity(), address.getState(), address.getPostalCode(), address.getCountryCode());
    }

    @Override
    public AddressResponseDTO updateAddress(CustomUserDetails currentUser, Long addressId, AddressRequestDTO request) {
        Address address = getOwnedAddress(currentUser, addressId);
        address.setId(addressId);
        address.setLabel(request.label());
        address.setStreetLine1(request.streetLine1());
        address.setStreetLine2(request.streetLine2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountryCode(request.countryCode());
        address = addressRepository.save(address);
        return new AddressResponseDTO(address.getId(), address.getLabel(), address.getStreetLine1(),
                address.getStreetLine2(), address.getCity(), address.getState(), address.getPostalCode(), address.getCountryCode());
    }

    @Override
    public void deleteAddress(CustomUserDetails currentUser, Long addressId) {
        addressRepository.delete(getOwnedAddress(currentUser, addressId));
    }

    private Address getOwnedAddress(CustomUserDetails currentUser, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(currentUser.user().getId())) {
            throw new AccessDeniedException("You do not have permission to access this address.");
        }
        return address;
    }
}
