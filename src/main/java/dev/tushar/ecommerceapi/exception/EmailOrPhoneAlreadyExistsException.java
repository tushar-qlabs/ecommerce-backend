package dev.tushar.ecommerceapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class EmailOrPhoneAlreadyExistsException extends ConflictException {

    private final String title;

    private EmailOrPhoneAlreadyExistsException(String title, String message) {
        super(message);
        this.title = title;
    }

    public static EmailOrPhoneAlreadyExistsException forEmail(String title, String email) {
        return new EmailOrPhoneAlreadyExistsException(title, "User with email " + email + " already exists");
    }

    public static EmailOrPhoneAlreadyExistsException forPhone(String title, String phone) {
        return new EmailOrPhoneAlreadyExistsException(title, "User with phone " + phone + " already exists");
    }

    public static EmailOrPhoneAlreadyExistsException forEmail(String email) {
        return new EmailOrPhoneAlreadyExistsException(null, "User with email " + email + " already exists");
    }

    public static EmailOrPhoneAlreadyExistsException forPhone(String phone) {
        return new EmailOrPhoneAlreadyExistsException(null, "User with phone " + phone + " already exists");
    }
}