package ru.samoylov.cloud_storage.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.samoylov.cloud_storage.dto.ValidPathDTO;

@Tag(name = "ресурсы", description = "работа с ресурсами хранилища")
@ApiResponse(responseCode = "500", description = "неизвесная ошибка", content = @Content(
        examples = @ExampleObject(
                value = "{\"message\": \"string\"}"
        )
))
@ApiResponse(responseCode = "401", description = "пользователь не авторизован", content = @Content(
        examples = @ExampleObject(
                value = "{\"message\": \"string\"}"
        )
))
public interface ResourceSwagger {

    @Operation(summary = "получение информации о ресурсе")
    @ApiResponse(responseCode = "200", description = "успешно", content = @Content(
            examples = @ExampleObject(
                    value = "{\n" +
                            "    \"path\": \"folder1/folder2/\", \n" +
                            "    \"name\": \"file.txt\",\n" +
                            "    \"size\": 123, \n" +
                            "    \"type\": \"FILE\" \n" +
                            "  }"
            )))
    @ApiResponse(responseCode = "400", description = "невалидный или отсутствующий путь", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "404", description = "ресурс не найден", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    public ResponseEntity<?> getResourceInfo(@Valid ValidPathDTO path);


    @Operation(summary = "удаление ресурса")
    @ApiResponse(responseCode = "204", description = "ок")
    @ApiResponse(responseCode = "400", description = "невалидный или отсутствующий путь", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "404", description = "ресурс не найден", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    public ResponseEntity<?> delete(@Valid ValidPathDTO path);

    @Operation(summary = "скачавинание")
    @ApiResponse(responseCode = "400", description = "невалидный или отсутствующий путь", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "404", description = "ресурс не найден", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    public ResponseEntity<?> download(@RequestParam(name = "path") String path,
                                      HttpServletResponse response);

    @Operation(summary = "переименование/перемещение")
    @ApiResponse(responseCode = "200", description = "успешно", content = @Content(
            examples = @ExampleObject(
                    value = "{\n" +
                            "    \"path\": \"folder1/folder2/\", \n" +
                            "    \"name\": \"file.txt\",\n" +
                            "    \"size\": 123, \n" +
                            "    \"type\": \"FILE\" \n" +
                            "  }"
            )))
    @ApiResponse(responseCode = "400", description = "невалидный или отсутствующий путь", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "404", description = "ресурс не найден", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "401", description = "пользователь не авторизован", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "409", description = "ресурс уже существует", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    public ResponseEntity<?> rename(
            @RequestParam(name = "from") String from,
            @RequestParam(name = "to") String to);



    @Operation(summary = "поиск")
    @ApiResponse(responseCode = "200", description = "успешно", content = @Content(
            examples = @ExampleObject(
                    value = "{\n" +
                            "    \"path\": \"folder1/folder2/\", \n" +
                            "    \"name\": \"file.txt\",\n" +
                            "    \"size\": 123, \n" +
                            "    \"type\": \"FILE\" \n" +
                            "  }"
            )))
    @ApiResponse(responseCode = "400", description = "невалидный или отсутствующий путь", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "401", description = "пользователь не авторизован", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    public ResponseEntity<?> search(@RequestParam(name = "query") String path);

    @Operation(summary = "аплоад")
    @ApiResponse(responseCode = "201", description = "успешно", content = @Content(
            examples = @ExampleObject(
                    value = "{\n" +
                            "    \"path\": \"folder1/folder2/\", \n" +
                            "    \"name\": \"file.txt\",\n" +
                            "    \"size\": 123, \n" +
                            "    \"type\": \"FILE\" \n" +
                            "  }"
            )))
    @ApiResponse(responseCode = "400", description = "невалидное тело запроса", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "401", description = "пользователь не авторизован", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    @ApiResponse(responseCode = "409", description = "ресурс уже существует", content = @Content(
            examples = @ExampleObject(
                    value = "{\"message\": \"string\"}"
            )
    ))
    public ResponseEntity<?> upload(
            @RequestParam("path") String folderPath,
            @RequestParam("object") MultipartFile[] files);
}
