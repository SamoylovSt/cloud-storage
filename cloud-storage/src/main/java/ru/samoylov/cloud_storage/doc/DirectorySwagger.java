package ru.samoylov.cloud_storage.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import ru.samoylov.cloud_storage.dto.MinioResourceInfo;
import ru.samoylov.cloud_storage.dto.ValidPathDTO;

import java.util.List;

@Tag(name = "папки", description = "работа с папками хранилища")
@ApiResponse(responseCode = "401", description = "пользователь не авторизован", content = @Content(
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
public interface DirectorySwagger {
    @Operation(summary = "получение информации о содержимом папки")
    @ApiResponse(responseCode = "200", description = "успешно", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\n" +
                            "    \"path\": \"folder1/folder2/\", \n" +
                            "    \"name\": \"file.txt\",\n" +
                            "    \"size\": 123, \n" +
                            "    \"type\": \"FILE\" \n" +
                            "  }"
            )))
    @ApiResponse(responseCode = "400", description = "невалидный или отсутствующий путь", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))

    @ApiResponse(responseCode = "404", description = "папка не существует", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    public ResponseEntity<List<MinioResourceInfo>> getInfoInFolder(@RequestParam(name = "path") String path);

    @Operation(summary = "создание пустой папки")
    @ApiResponse(responseCode = "201", description = "успешно", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\n" +
                            "  \"path\": \"folder1/folder2/\",\n" +
                            "  \"name\": \"folder3\",\n" +
                            "  \"type\": \"DIRECTORY\"\n" +
                            "}"
            )))
    @ApiResponse(responseCode = "400", description = "невалидный или отсутствующий путь к новой папке", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "404", description = "родительская папка не существует", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "409", description = "папка уже существует", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    public ResponseEntity<?> createFolder(@Valid ValidPathDTO path);

}
