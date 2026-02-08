package ru.samoylov.cloud_storage.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "пользователь", description = "получение текущего пользователя")
@ApiResponse(responseCode = "200", description = "успешно", content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(
                value = "{\"username\": \"user_1\"}"
        )))
@ApiResponse(responseCode = "401", description = "неверные данные", content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(
                value = "{\"message\": \"string\"}"
        )
))
@ApiResponse(responseCode = "500", description = "неизвесная ошибка", content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(
                value = "{\"message\": \"string\"}"
        )
))
public interface UserSwagger {
    @Operation(summary = "получение текущего пользователя")
    public ResponseEntity<?> getUser();
}
