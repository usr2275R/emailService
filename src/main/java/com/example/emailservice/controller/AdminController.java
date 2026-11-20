package com.example.emailservice.controller;

import com.example.emailservice.repositary.EmailRecordRepositary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {


    private final EmailRecordRepositary repository;

    @GetMapping("/test")
    public String test() { return "admin ok"; }


}