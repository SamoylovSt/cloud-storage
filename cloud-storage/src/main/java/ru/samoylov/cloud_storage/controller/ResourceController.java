package ru.samoylov.cloud_storage.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.samoylov.cloud_storage.doc.ResourceSwagger;
import ru.samoylov.cloud_storage.dto.MinioResourceInfo;
import ru.samoylov.cloud_storage.dto.ValidPathDTO;
import ru.samoylov.cloud_storage.service.MinioService;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController implements ResourceSwagger {

    @Autowired
    private MinioService minioService;

    @Override
    @DeleteMapping
    public ResponseEntity<?> delete(@Valid ValidPathDTO path) {
        minioService.deleteResource(path.getPath());
        return ResponseEntity.status(204).build();
    }

    @Override
    @GetMapping
    public ResponseEntity<?> getResourceInfo(@Valid ValidPathDTO path) {
        return ResponseEntity.ok(minioService.getResourceInfo(path.getPath()));
    }

    @Override
    @PostMapping
    public ResponseEntity<?> upload(
            @RequestParam("path") String folderPath,
            @RequestParam("object") MultipartFile[] files) {

        List<MinioResourceInfo> uploadedFiles = minioService.uploadMultiple(folderPath, files);
        return ResponseEntity.status(201).body(uploadedFiles);
    }

    @Override
    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam(name = "path") String path,
                                      HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        minioService.download(path, response);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(name = "query") String path) {
        List<MinioResourceInfo> minioResource = minioService.search(path);
        return ResponseEntity.ok().body(minioResource);
    }

    @Override
    @GetMapping("/move")
    public ResponseEntity<?> rename(
            @RequestParam(name = "from") String from,
            @RequestParam(name = "to") String to) {
        MinioResourceInfo result = minioService.rename(from, to);
        return ResponseEntity.ok(result);
    }

}
