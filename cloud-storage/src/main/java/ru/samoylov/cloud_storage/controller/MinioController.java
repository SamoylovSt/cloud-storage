package ru.samoylov.cloud_storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<?> upload(@RequestParam(name = "path") String path,
                                    MultipartFile file
    ) {
        return ResponseEntity.ok(minioService.uploadResource(path, file));
    }

}
