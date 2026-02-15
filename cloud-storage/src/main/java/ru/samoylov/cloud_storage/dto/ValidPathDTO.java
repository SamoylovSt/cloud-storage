package ru.samoylov.cloud_storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidPathDTO {
    @NotBlank(message = "невалидный или отсутствующий путь")
    @Size(min = 0, max = 100, message = "невалидный или отсутствующий путь")
    @Pattern(
            regexp = "^(?!.*//)" +
                    "(?!.*\\\\)" +
                    "(?!.*\\.\\.)" +
                    "(?!.*\\*)" +
                    "(?!.*\\?)" +
                    "(?!.*\")" +
                    "(?!.*:)" +
                    "(?!.*[<>|])" +
                    "[^\\\\/*?\"<>|:]*$",
            message = "невалидный или отсутствующий путь"
    )
    private String path;
}
