package com.projeto.service;

import com.projeto.model.Register;
import com.projeto.repository.RegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterService {

    private final RegisterRepository registerRepository;

    @Autowired
    public RegisterService(RegisterRepository registerRepository) {
        this.registerRepository = registerRepository;
    }

    public Page<Register> findAll(Register.RegisterStatus status, @NonNull Pageable pageable) {
        if (status != null) {
            return registerRepository.findByStatus(status, pageable);
        }
        return registerRepository.findAll(pageable);
    }

    @Transactional
    public Register save(@NonNull Register register) {
        return registerRepository.save(register);
    }
}
