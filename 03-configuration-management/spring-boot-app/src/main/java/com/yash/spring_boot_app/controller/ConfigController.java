package com.yash.spring_boot_app.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConfigController {

    @Value("${APP_NAME:Default App}")
    private String appName;

    @Value("${ENVIRONMENT:local}")
    private String environment;

    @Value("${DB_USERNAME:unknown}")
    private String dbUsername;

    @GetMapping("/config")
    public Map<String, String> getConfig() {

        return Map.of(
                "appName", appName,
                "environment", environment,
                "dbUsername", dbUsername

        );
    }

}
