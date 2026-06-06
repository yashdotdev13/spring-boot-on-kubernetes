package com.yash.spring_boot_app.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HelloController {


    @PostMapping("/hello")
    public Map<String, String> hello(){
        return Map.of(
                "message",
                "Hello from spring boot"
        );
    }
}
