package ru.samoylov.cloud_storage.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 30, message = "Username is not correct")
    private String username;
    @NotBlank(message = "password cannot be empty")
    @Size(min = 5, max = 100, message = "password is not correct")
    private String password;


}
