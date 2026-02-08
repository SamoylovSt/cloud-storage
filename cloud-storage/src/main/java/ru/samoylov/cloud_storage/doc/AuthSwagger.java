package ru.samoylov.cloud_storage.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import ru.samoylov.cloud_storage.dto.RegisterRequestDTO;

@Tag(name = "Авторизация", description = "регистрация/авторизация пользователей с исользованием Spring security")
@ApiResponse(responseCode = "500", description = "неизвесная ошибка", content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(
                value = "{\"message\": \"string\"}"
        )
))
public interface AuthSwagger {
    @Operation(summary = "вход")
    @ApiResponse(responseCode = "200", description = "успешно", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"username\": \"string\"}"
            )))
    @ApiResponse(responseCode = "400", description = "ошибки валидации", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "401", description = "неверные данные", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody RegisterRequestDTO authRequest);

    @Operation(summary = "регистрация")
    @ApiResponse(responseCode = "201", description = "успешно", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"username\": \"string\"}"
            )))
    @ApiResponse(responseCode = "400", description = "ошибки валидации", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "409", description = "username занят", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest);

    @Operation(summary = "выход")
    @ApiResponse(responseCode = "204")
    @ApiResponse(responseCode = "401", description = "запрос исполняется неавторизованным юзером", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )))
    public ResponseEntity<?> logoutUser();
}
