package com.projeto.automation.service;

import com.projeto.automation.model.AutomationResult;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * SERVIÇO DE AUTOMAÇÃO DE IA (Simulação)
 * 
 * Este serviço simula o comportamento de um motor de IA que analisa
 * os produtos e atribui um status de risco, além de fornecer uma
 * galeria de imagens de alta qualidade para produtos específicos.
 */
@Service
public class MLAutomationService {

    // Listas de imagens locais para os produtos principais
    // Inclui as imagens originais (.org) e variações configuradas pelo usuário.
    private static final List<String> IMG_IPHONE = Arrays.asList(
            "/img/iphoneorg.webp", "/img/iphone.webp", "/img/iphone2.webp", "/img/iphone3.webp");

    private static final List<String> IMG_SAMSUNG = Arrays.asList(
            "/img/S25org.webp", "/img/S25 ultra.webp", "/img/S25 ultra2.webp", "/img/S25 ultra 3.webp");

    private static final List<String> IMG_MONITOR = Arrays.asList(
            "/img/monitororg.webp", "/img/monitor.webp", "/img/monitor1.webp", "/img/monitor3.webp");

    private static final String IMG_PS5 = "https://placehold.co/600x400/png?text=PlayStation+5";
    private static final String IMG_GENERICO = "/img/monitor3.webp";

    /**
     * Analisa o produto com base no ID ou Descrição.
     * 
     * @param id        ID do Mercado Livre (ex: MLB-500)
     * @param descricao Texto descritivo do produto
     * @return AutomationResult contendo a lista de imagens e o status de risco (IA)
     */
    public AutomationResult analisarProduto(String id, String descricao) {
        String produtoIdentificado = null;

        // 1. Identificação via ID (Padrão MLB)
        if (id != null) {
            switch (id.toUpperCase()) {
                case "MLB-100":
                case "MLB-200":
                case "MLB-300":
                    produtoIdentificado = "PS5";
                    break;
                case "MLB-500":
                case "MLB-600":
                    produtoIdentificado = "IPHONE";
                    break;
                case "MLB-800":
                case "MLB-900":
                    produtoIdentificado = "SAMSUNG";
                    break;
            }
        }

        // 2. Identificação via Palavras-chave na descrição (Fallback)
        if (produtoIdentificado == null && descricao != null) {
            String descLower = descricao.toLowerCase();
            if (descLower.contains("iphone") && descLower.contains("15")) {
                produtoIdentificado = "IPHONE";
            } else if (descLower.contains("ps5") || descLower.contains("playstation")) {
                produtoIdentificado = "PS5";
            } else if (descLower.contains("s25") || descLower.contains("ultra") || descLower.contains("samsung")) {
                produtoIdentificado = "SAMSUNG";
            } else if (descLower.contains("monitor") || descLower.contains("dell")) {
                produtoIdentificado = "MONITOR";
            }
        }

        // 3. Atribuição de Resultado (Simulando análise de IA)
        // ALTO RISCO: Produtos eletrônicos de alto valor unitário
        // BAIXO RISCO: Itens de menor valor ou específicos (Monitor)
        if ("PS5".equals(produtoIdentificado)) {
            return new AutomationResult(Collections.singletonList(IMG_PS5), "ALTO RISCO");
        } else if ("IPHONE".equals(produtoIdentificado)) {
            return new AutomationResult(IMG_IPHONE, "ALTO RISCO");
        } else if ("SAMSUNG".equals(produtoIdentificado)) {
            return new AutomationResult(IMG_SAMSUNG, "ALTO RISCO");
        } else if ("MONITOR".equals(produtoIdentificado)) {
            return new AutomationResult(IMG_MONITOR, "BAIXO RISCO");
        } else {
            // Caso nenhum produto seja identificado, retorna o genérico
            return new AutomationResult(Collections.singletonList(IMG_GENERICO), "BAIXO RISCO");
        }
    }
}
