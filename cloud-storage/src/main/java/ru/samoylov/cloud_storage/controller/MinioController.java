package ru.samoylov.cloud_storage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.samoylov.cloud_storage.dto.MinioResource;
import ru.samoylov.cloud_storage.service.MinioService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MinioController {

    @Autowired
    private MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping("/directory")
    public ResponseEntity<List<MinioResource>> getInfoInFolder(@RequestParam(name = "path") String path) {
        return ResponseEntity.ok(minioService.getInfoInFolder(path));
    }

    @PostMapping("/directory")
    public ResponseEntity<?> createFolder(@RequestParam(name = "path") String path) {
        return ResponseEntity.ok(minioService.createFolder(path));
    }

    @DeleteMapping("/resource")
    public ResponseEntity<?> delete(@RequestParam(name = "path") String path) {
        minioService.deleteResource(path);
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/resource")
    public ResponseEntity<?> getResourceInfo(@RequestParam(name = "path") String path) {
        return ResponseEntity.ok(minioService.getResourceInfo(path));
    }


    @PostMapping("/resource")
    public ResponseEntity<?> upload(
            @RequestParam("path") String folderPath,
            @RequestParam("object") MultipartFile[] files) {

        List<MinioResource> uploadedFiles = minioService.uploadMultiple(folderPath, files);
        return ResponseEntity.status(201).body(uploadedFiles);
    }

    @GetMapping("/resource/download")
    public ResponseEntity<?> download(@RequestParam(name = "path") String path,
                                      HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        minioService.download(path, response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/resource/search")
    public ResponseEntity<?> search(@RequestParam(name = "query") String path) {

        List<MinioResource> minioResource = minioService.search(path);
        return ResponseEntity.ok().body(minioResource);
    }

}
