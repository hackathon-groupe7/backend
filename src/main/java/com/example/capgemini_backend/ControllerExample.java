package com.example.capgemini_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerExample {
    @GetMapping("/")
    public String hello() {
    return "Hello World!";
    }
}
