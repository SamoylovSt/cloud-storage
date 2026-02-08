package ru.samoylov.cloud_storage.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.samoylov.cloud_storage.doc.DirectorySwagger;
import ru.samoylov.cloud_storage.dto.MinioResourceInfo;
import ru.samoylov.cloud_storage.dto.ValidPathDTO;
import ru.samoylov.cloud_storage.service.MinioService;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
public class DirectoryController implements DirectorySwagger {

    private final MinioService minioService;

    public DirectoryController(MinioService minioService) {
        this.minioService = minioService;
    }

    @Override
    @GetMapping
    public ResponseEntity<List<MinioResourceInfo>> getInfoInFolder(@RequestParam(name = "path") String path) {
        return ResponseEntity.ok(minioService.getInfoInFolder(path));
    }

    @Override
    @PostMapping
    public ResponseEntity<?> createFolder(@Valid ValidPathDTO path) {
        return ResponseEntity.ok(minioService.createFolder(path.getPath()));
    }

}
