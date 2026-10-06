package com.webapp.shoppingwebsite.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runtime settings for the Angular app. The app loads this once at startup, so these values
 * can be changed with environment variables instead of rebuilding the Angular bundle.
 * Only values that are safe to show in the browser belong here.
 */
@RestController
public class FrontendConfigController {

    @Value("${frontend.recommendationApiUrl}")
    private String recommendationApiUrl;

    @Value("${frontend.geoapifyApiKey}")
    private String geoapifyApiKey;

    @GetMapping("/app-config")
    public Map<String, String> getConfig() {
        Map<String, String> config = new LinkedHashMap<>();
        config.put("recommendationApiUrl", recommendationApiUrl);
        config.put("geoapifyApiKey", geoapifyApiKey);
        return config;
    }
}
