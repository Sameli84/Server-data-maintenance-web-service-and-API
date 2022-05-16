package com.example.servermaintenance.account;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class RegisterDTO {
    @NotEmpty(message = "First name is required")
    private String firstName;

    @NotEmpty(message = "Last name is required")
    private String lastName;

    @NotEmpty(message = "Email is required")
    @Email(message = "Must be a valid email")
    @Pattern(regexp = ".*(@tuni.fi)$", message = "Must use an email from an authorized provider")
    private String email;

    @NotEmpty(message = "Password is required")
    @Size(min = 8, message = "Password should be atleast 8 characters long")
    private String password;
}
