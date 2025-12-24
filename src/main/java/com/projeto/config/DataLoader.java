package com.projeto.config;

import com.projeto.model.Register;
import com.projeto.model.User;
import com.projeto.repository.RegisterRepository;
import com.projeto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RegisterRepository registerRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [DATA LOADER] Iniciando carga de dados de demonstração...");

        long count = registerRepository.count();
        if (count >= 20) {
            System.out.println(">>> [DATA LOADER] Banco já possui " + count + " itens. Verificando regra de 80%...");
            registerRepository.findAll().forEach(reg -> {
                BigDecimal expected = reg.getSalePrice().multiply(new BigDecimal("0.8")).setScale(2,
                        java.math.RoundingMode.HALF_UP);
                if (reg.getCompensationPrice() == null || reg.getCompensationPrice()
                        .setScale(2, java.math.RoundingMode.HALF_UP).compareTo(expected) != 0) {
                    reg.setCompensationPrice(expected);
                    registerRepository.save(reg);
                }
            });
            System.out.println(">>> [DATA LOADER] Verificação e ajuste de preços concluído.");
            return;
        }

        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            System.out.println(">>> [DATA LOADER] Criando usuário administrador padrão...");
            User newUser = new User();
            newUser.setUsername("admin");
            newUser.setPassword("admin123");
            newUser.setFullName("Lidiane Loss");
            newUser.setRole(User.UserRole.ADMIN);
            return userRepository.save(newUser);
        });

        String[] productNames = {
                "iPhone 15 Pro Max", "Controle PS5 DualSense", "Fone Sony WH-1000XM5",
                "Notebook Dell G15", "Teclado Mecânico Razer", "Monitor Gamer 144Hz",
                "Cafeteira Nespresso", "Air Fryer Mondial", "Aspirador Robô",
                "Tênis Nike Air Force", "Moletom Adidas Original", "Relógio Casio Classic",
                "Barra de Chocolate Milka", "Pacote Café Gourmet", "Vinho Tinto Reserva",
                "Console Nintendo Switch", "Smartwatch Apple Series 9", "Câmera Canon EOS",
                "Mochila Antifurto", "Kit Ferramentas Profissional"
        };

        String[] colors = { "Preto", "Branco", "Azul", "Cinza", "Vermelho", "Dourado", "Prata" };
        String[] categories = { "ELETRONICOS", "CASA", "MODA", "ALIMENTICIO", "GAMER" };
        java.util.Random random = new java.util.Random();

        System.out.println(">>> [DATA LOADER] Gerando 40 produtos aleatórios com regra de 80% de indenização...");
        for (int i = 0; i < 40; i++) {
            int floorVal = random.nextInt(5); // 0-4
            int streetVal = 100 + (random.nextInt(21) * 10); // 100, 110, ..., 300
            int posVal = random.nextInt(5) + 1; // 1-5
            String subPosVal = "00";

            if (posVal == 2 || posVal == 3) {
                subPosVal = String.format("%02d", random.nextBoolean() ? 1 : 2); // 01 or 02
            }

            String formattedPos = String.format("%02d", posVal);
            String locationStr = String.format("AN %d-%d-%s-%s", floorVal, streetVal, formattedPos, subPosVal);

            BigDecimal salePrice = new BigDecimal(50 + random.nextInt(2000)).setScale(2,
                    java.math.RoundingMode.HALF_UP);
            BigDecimal compensationPrice = salePrice.multiply(new BigDecimal("0.8")).setScale(2,
                    java.math.RoundingMode.HALF_UP);

            Register reg = new Register();
            reg.setProductName(productNames[random.nextInt(productNames.length)]);
            reg.setColor(colors[random.nextInt(colors.length)]);
            reg.setSku("789" + (100000000 + i));
            reg.setFloor(String.valueOf(floorVal));
            reg.setStreet(String.valueOf(streetVal));
            reg.setPosition(formattedPos);
            reg.setSubPosition(subPosVal);
            reg.setLocation(locationStr);
            reg.setIdentifierId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            reg.setSellerId("S" + (2000 + i));
            reg.setMeliId("MLB" + (3000000 + i));
            reg.setSalePrice(salePrice);
            reg.setCompensationPrice(compensationPrice);
            reg.setType(Register.RegisterType.ENTRY);
            reg.setRegisteredBy(admin);
            reg.setStatus(Register.RegisterStatus.PENDING);
            reg.setTimestamp(LocalDateTime.now().minusDays(random.nextInt(100))); // Varied priority levels
            reg.setProtocol(UUID.randomUUID().toString());
            reg.setDescription("Demo Categoria: " + categories[random.nextInt(categories.length)]);

            registerRepository.save(reg);
        }
        System.out.println(">>> [DATA LOADER] Carga concluída com sucesso! 40 produtos inseridos.");
    }
}
