package ru.samoylov.cloud_storage.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.samoylov.cloud_storage.dto.MinioResourceInfo;
import ru.samoylov.cloud_storage.dto.ValidPathDTO;

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
    public ResponseEntity<List<MinioResourceInfo>> getInfoInFolder(@RequestParam(name = "path") String path) {
        return ResponseEntity.ok(minioService.getInfoInFolder(path));
    }

    @PostMapping("/directory")
    public ResponseEntity<?> createFolder(@Valid ValidPathDTO path) {
        return ResponseEntity.ok(minioService.createFolder(path.getPath()));
    }

    @DeleteMapping("/resource")
    public ResponseEntity<?> delete(@Valid ValidPathDTO path) {
        minioService.deleteResource(path.getPath());
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/resource")
    public ResponseEntity<?> getResourceInfo(@Valid ValidPathDTO path) {
        return ResponseEntity.ok(minioService.getResourceInfo(path.getPath()));
    }


    @PostMapping("/resource")
    public ResponseEntity<?> upload(
            @RequestParam("path") String folderPath,
            @RequestParam("object") MultipartFile[] files) {

        List<MinioResourceInfo> uploadedFiles = minioService.uploadMultiple(folderPath, files);
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
        List<MinioResourceInfo> minioResource = minioService.search(path);
        return ResponseEntity.ok().body(minioResource);
    }

    @GetMapping("/resource/move")
    public ResponseEntity<?> rename(
        @RequestParam(name = "from") String from,
        @RequestParam(name = "to") String to) {

        System.out.println(from + " from");
        System.out.println(to + "to");
        MinioResourceInfo result = minioService.rename(from, to);
        return ResponseEntity.ok(result);
    }

}
