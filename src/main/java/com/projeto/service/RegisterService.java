package com.projeto.service;

import com.projeto.model.Register;
import com.projeto.repository.RegisterRepository;
import com.projeto.specification.RegisterSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    public Page<Register> findAll(Register.RegisterStatus status, String search, String floor, String sector,
            String street, String risk, String valueRange, @NonNull Pageable pageable) {
        Specification<Register> spec = RegisterSpecification.withFilters(search, floor, sector, street, risk,
                valueRange, status);
        return registerRepository.findAll(spec, pageable);
    }

    @Transactional
    public Register save(@NonNull Register register) {
        return registerRepository.save(register);
    }
}
