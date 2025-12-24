package com.projeto.controller;

import com.projeto.model.Register;
import com.projeto.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registers")
@CrossOrigin("*")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @GetMapping
    public ResponseEntity<Page<Register>> findAll(@RequestParam(required = false) Register.RegisterStatus status,
            Pageable pageable) {
        Pageable safePageable = pageable != null ? pageable : Pageable.unpaged();
        return ResponseEntity.ok(registerService.findAll(status, safePageable));
    }

    @PostMapping
    public ResponseEntity<Register> create(@RequestBody Register register) {
        if (register == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(registerService.save(register));
    }
}
