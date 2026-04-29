
package com.example.demo.controller;

import com.example.demo.model.HistorialClinico;
import com.example.demo.repository.HistorialRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/historial")
@CrossOrigin(origins = "http://localhost:4200")
public class HistorialController {
    @Autowired
    private HistorialRepository repo;

    @PostMapping
    public HistorialClinico save(@RequestBody HistorialClinico h){
        return repo.save(h);
    }

    @GetMapping
    public List<HistorialClinico> listar(){
        return repo.findAll();
    }
}
