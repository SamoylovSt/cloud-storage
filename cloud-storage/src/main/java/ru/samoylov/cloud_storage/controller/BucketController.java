package ru.samoylov.cloud_storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.samoylov.cloud_storage.dto.MinioResourceInfo;
import ru.samoylov.cloud_storage.service.MinioService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BucketController {


    @Autowired
    private MinioService minioService;

    public BucketController(MinioService minioService) {
        this.minioService = minioService;
    }


    @PostMapping("/directory")
    public ResponseEntity<?> createFolder(@RequestParam(name = "path") String path) {
        String normalizedPath = path;
        if (!normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath + "/";
        }
        minioService.createFolder(normalizedPath);
        String folderName= minioService.extractFolderName(normalizedPath);

        Map<String, String> response = new HashMap<>();
        response.put("path", normalizedPath);
        response.put("name", folderName);
        response.put("type", "DIRECTORY");
        return ResponseEntity.ok(response);

    }

    @GetMapping("/resource")
    public ResponseEntity<?> getInfoAboutFile(@RequestParam(name = "path") String file) {
        MinioResourceInfo resourceInfo = minioService.getResourceInfo(file);
        return ResponseEntity.ok(resourceInfo);
        //TODO сейчас получает информауию только по полному имени файла с расширением
    }

    @DeleteMapping("/resource")
    public ResponseEntity<?> deleteResource(@RequestParam(name = "path") String path) {
        minioService.deleteResource(path);
        return ResponseEntity.ok(204);
        //TODO не проверено удаление
    }
//
//    @GetMapping("/directory")
//    public ResponseEntity<?> getInfoAboutFolder(@RequestParam(name = "path") String path) {
//
//        return ResponseEntity.ok(minioService.getResourceInFolder(path));
//
//    }
}
