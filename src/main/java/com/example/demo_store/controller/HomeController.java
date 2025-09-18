package com.example.demo_store.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Demo Store API is running");
        body.put("docs", "/api/test/hello");
        return ResponseEntity.ok(body);
    }
}


