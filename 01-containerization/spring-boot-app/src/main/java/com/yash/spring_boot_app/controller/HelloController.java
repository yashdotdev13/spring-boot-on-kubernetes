package com.yash.spring_boot_app.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HelloController {

    @Value("${app.message}")
    private String message;

//    @PostMapping("/hello")
//    public Map<String, String> hello(){
//        return Map.of(
//                "message",
//                "Hello from spring boot"
//        );
//    }

        @GetMapping("/hello")
        public Map<String, String> hello() {
            return Map.of("message", message);
        }
    }
