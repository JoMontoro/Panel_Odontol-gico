package com.example.demo.controller;

import com.example.demo.dto.DashboardDTO;
import com.example.demo.service.DashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    private DashboardService service;

    @GetMapping
    public DashboardDTO obtenerDashboard(){

        return service.obtenerDashboard();

    }

}