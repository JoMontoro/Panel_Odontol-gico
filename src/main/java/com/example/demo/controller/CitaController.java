
package com.example.demo.controller;

import com.example.demo.model.Cita;
import com.example.demo.service.CitaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


public class CitaController {
    @Autowired
    private CitaService service;

    @PostMapping
    public Cita save(@RequestBody Cita c){
        return service.save(c);
    }

    @GetMapping
    public List<Cita> listar(){
        return service.listar();
    }
}
