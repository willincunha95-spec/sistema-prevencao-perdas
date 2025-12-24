package com.projeto.config;

import com.projeto.model.*;
import com.projeto.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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

    private final Random random = new Random();
    private final String[] PRODUCTS = {
            "PlayStation 5", "iPhone 15 Pro", "Notebook Dell", "Geladeira Samsung",
            "Smart TV LG 55", "Fone Bluetooth", "Cadeira Gamer", "Monitor 27",
            "Teclado Mecânico", "Mouse Sem Fio", "Tablet Samsung", "iPad Air",
            "Airfryer Mundial", "Microondas Electrolux", "Ventilador Arno", "Batedeira Philip"
    };
    private final String[] LOCATIONS = { "A-01", "B-02", "C-03", "Depósito", "Recebimento", "Docas" };
    private final String[] FLOORS = { "1", "2", "3" };
    private final String[] STREETS = { "10", "20", "30", "40", "50" };

    @Override
    public void run(String... args) throws Exception {
        try {
            if (registerRepository.count() > 100) {
                System.out.println("DataSeeder: Database already populated. Skipping.");
                return;
            }

            System.out.println("DataSeeder: Starting simulation of 500 products...");

            User admin = userRepository.findByUsername("admin").orElseGet(() -> {
                User u = new User();
                u.setUsername("admin");
                u.setFullName("Administrador Sistema");
                u.setPassword("admin123");
                u.setRole(User.UserRole.ADMIN);
                return userRepository.save(u);
            });

            int total = 500;
            List<Register> registers = new ArrayList<>();
            List<Analysis> analyses = new ArrayList<>();
            List<Found> founds = new ArrayList<>();
            List<DFL> dfls = new ArrayList<>();

            for (int i = 0; i < total; i++) {
                int type = random.nextInt(4); // 0=Register, 1=Analysis, 2=Found, 3=DFL

                String name = PRODUCTS[random.nextInt(PRODUCTS.length)] + " - "
                        + UUID.randomUUID().toString().substring(0, 5);
                BigDecimal price = new BigDecimal(random.nextInt(5000) + 50);
                String loc = LOCATIONS[random.nextInt(LOCATIONS.length)];

                if (type == 0 || type > 3) { // Prefer Registers (Pending)
                    Register r = new Register();
                    r.setProductName(name);
                    r.setSalePrice(price);
                    r.setCompensationPrice(price.multiply(new BigDecimal("0.8")));
                    r.setLocation(loc);
                    r.setFloor(FLOORS[random.nextInt(FLOORS.length)]);
                    r.setStreet(STREETS[random.nextInt(STREETS.length)]);
                    r.setSku("SKU-" + random.nextInt(999999));
                    r.setType(Register.RegisterType.ENTRY);
                    r.setRegisteredBy(admin);
                    registers.add(r);
                } else if (type == 1) {
                    Analysis a = new Analysis();
                    a.setTitle(name);
                    a.setSalePrice(price);
                    a.setCompensationPrice(price.multiply(new BigDecimal("0.8")));
                    a.setResponsible(admin);
                    a.setStatus(Analysis.AnalysisStatus.PENDING);
                    a.setFloor(FLOORS[random.nextInt(FLOORS.length)]);
                    a.setStreet(STREETS[random.nextInt(STREETS.length)]);
                    analyses.add(a);
                } else if (type == 2) {
                    Found f = new Found();
                    f.setItemName(name);
                    f.setLocationFound(loc);
                    f.setFoundBy(admin);
                    f.setStatus(Found.FoundStatus.STORED);
                    f.setFloor(FLOORS[random.nextInt(FLOORS.length)]);
                    f.setStreet(STREETS[random.nextInt(STREETS.length)]);
                    founds.add(f);
                } else {
                    DFL d = new DFL();
                    d.setProductName(name);
                    d.setQuantity(1);
                    d.setUnitValue(price);
                    d.setSku("SKU-" + random.nextInt(999999));
                    d.setReportedBy(admin);
                    d.setType(DFL.DFLType.BREAKAGE);
                    d.setFloor(FLOORS[random.nextInt(FLOORS.length)]);
                    d.setStreet(STREETS[random.nextInt(STREETS.length)]);
                    dfls.add(d);
                }

                if (registers.size() >= 500) {
                    registerRepository.saveAll(registers);
                    registers.clear();
                }
                if (analyses.size() >= 500) {
                    analysisRepository.saveAll(analyses);
                    analyses.clear();
                }
                if (founds.size() >= 500) {
                    foundRepository.saveAll(founds);
                    founds.clear();
                }
                if (dfls.size() >= 500) {
                    dflRepository.saveAll(dfls);
                    dfls.clear();
                }
            }

            // Save remaining
            registerRepository.saveAll(registers);
            analysisRepository.saveAll(analyses);
            foundRepository.saveAll(founds);
            dflRepository.saveAll(dfls);

            System.out.println("DataSeeder: Simulation complete!");
        } catch (Exception e) {
            System.err.println("DataSeeder ERROR: Falha ao popular dados. A aplicação continuará rodando.");
            e.printStackTrace();
        }
    }
}
