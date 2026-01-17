package com.projeto.config;

import com.projeto.model.User;

import com.projeto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [DATA LOADER] Verificando dados...");

        userRepository.findByUsername("admin").orElseGet(() -> {
            System.out.println(">>> [DATA LOADER] Criando usuário administrador padrão...");
            User newUser = new User();
            newUser.setUsername("admin");
            newUser.setPassword("admin123");
            newUser.setFullName("Lidiane Loss");
            newUser.setRole(User.UserRole.ADMIN);
            return userRepository.save(newUser);
        });

        System.out.println(">>> [DATA LOADER] Verificação concluída. Nenhum produto novo será criado.");
    }
}
