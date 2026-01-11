package ru.samoylov.cloud_storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.samoylov.cloud_storage.service.MinioService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BucketController {


    private MinioService minioService;

    public BucketController(MinioService minioService) {
        this.minioService = minioService;
    }

//    @PostMapping("/directory")
//    public ResponseEntity<?> createFolder(@RequestParam(name = "path") String path) {
//        minioService.createFolder(path);
//
//        Map<String,String> response=new HashMap<>();
//        response.put("path")
//
//    }
}
