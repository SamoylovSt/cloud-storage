package ru.samoylov.cloud_storage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping(value = {
            "/",
            "/login",
            "/register",
            "/files",
            "/files/**",
            "/profile",
            "/settings",
            "/{path:[^\\.]+}"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}