package com.yash.spring_boot_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Hello from Spring Boot V2";
    }

    @GetMapping("/version")
    public String version() {
        return "Spring Boot App Version 2.0";
    }

}
