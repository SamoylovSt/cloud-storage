package ru.samoylov.cloud_storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

}
