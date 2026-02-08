package ru.samoylov.cloud_storage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.samoylov.cloud_storage.doc.UserSwagger;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController implements UserSwagger {
    @Override
    @GetMapping("/me")
    public ResponseEntity<?> getUser() {

        String name = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        Map<String, String> response = new HashMap<>();
        response.put("username", name);
        return ResponseEntity.ok(response);
    }
}
