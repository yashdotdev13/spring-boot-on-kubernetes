package com.yash.spring_boot_app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProfileController {

    @Value("${app.message}")
    private String message;

    @Value("${app.owner}")
    private String owner;

    @Value("${app.version}")
    private String version;


    @GetMapping("/profile")
    public Map<String,String> getProfile(){

        return Map.of(
                "message", message,
                "owner", owner,
                "version", version
        );
    }

}