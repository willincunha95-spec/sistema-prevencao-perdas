package com.projeto.config;

import com.projeto.model.*;
import com.projeto.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * SEMEADOR DE DADOS (DataSeeder.java)
 * 
 * Responsável por popular o banco de dados inicial na primeira execução.
 * Aqui garantimos que os produtos principais (iPhone, Samsung, Monitor)
 * existam para que o funcionário possa testar o sistema.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RegisterRepository registerRepository;
    @Autowired
    private AnalysisRepository analysisRepository;
    @Autowired
    private FoundRepository foundRepository;
    @Autowired
    private DFLRepository dflRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Verifica se já existem dados para evitar duplicidade
            if (registerRepository.count() > 0)
                return;

            // Criação do usuário administrador padrão
            User admin = userRepository.findByUsername("admin").orElseGet(() -> {
                User u = new User();
                u.setUsername("admin");
                u.setFullName("Administrador Sistema");
                u.setPassword("admin123");
                u.setRole(User.UserRole.ADMIN);
                return userRepository.save(u);
            });

            List<Register> registers = new ArrayList<>();

            // PRODUTO 1: iPhone 15
            // Note que usamos valores em Reais (R$), mas o frontend converterá para Dólar.
            Register r1 = new Register();
            r1.setProductName("iPhone 15");
            r1.setSalePrice(new BigDecimal("7299.00"));
            r1.setCompensationPrice(new BigDecimal("5839.20"));
            r1.setLocation("DV-10-01-01-00");
            r1.setFloor("1");
            r1.setStreet("10");
            r1.setSku("SKU-IPHONE15");
            r1.setType(Register.RegisterType.ENTRY);
            r1.setRegisteredBy(admin);
            r1.setStatus(Register.RegisterStatus.PENDING);
            r1.setDescription("Smartphone Apple iPhone 15 128GB");
            registers.add(r1);

            // PRODUTO 2: Monitor DELL
            Register r2 = new Register();
            r2.setProductName("Monitor DELL");
            r2.setSalePrice(new BigDecimal("1200.00"));
            r2.setCompensationPrice(new BigDecimal("960.00"));
            r2.setLocation("MTU-TT-77b");
            r2.setFloor("2");
            r2.setStreet("20");
            r2.setSku("SKU-MONITORDELL");
            r2.setType(Register.RegisterType.ENTRY);
            r2.setRegisteredBy(admin);
            r2.setStatus(Register.RegisterStatus.PENDING);
            r2.setDescription("Monitor Dell 24 polegadas Full HD");
            registers.add(r2);

            // PRODUTO 3: Samsung S25 Ultra
            Register r3 = new Register();
            r3.setProductName("Samsung S25 Ultra");
            r3.setSalePrice(new BigDecimal("8999.00"));
            r3.setCompensationPrice(new BigDecimal("7199.20"));
            r3.setLocation("HV-30-03-01-00");
            r3.setFloor("3");
            r3.setStreet("30");
            r3.setSku("SKU-S25ULTRA");
            r3.setType(Register.RegisterType.ENTRY);
            r3.setRegisteredBy(admin);
            r3.setStatus(Register.RegisterStatus.PENDING);
            r3.setDescription("Smartphone Samsung Galaxy S25 Ultra 512GB");
            registers.add(r3);

            // Salva todos os produtos iniciais
            registerRepository.saveAll(registers);
            System.out.println("DataSeeder: 3 produtos principais carregados com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
