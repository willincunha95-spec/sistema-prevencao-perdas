package com.projeto.automation.controller;

import com.projeto.automation.model.AutomationResult;
import com.projeto.automation.model.MLAnalysisResult;
import com.projeto.automation.model.MLProduct;
import com.projeto.automation.service.MLAutomationService;
import com.projeto.automation.service.MLProductScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLADOR PRINCIPAL DA AUTOMAÇÃO (AI & SCRAPING)
 * 
 * Este controller é o coração das novas funcionalidades do projeto.
 * Ele une a busca real do Mercado Livre com a inteligência simulada de IA.
 */
@RestController
@RequestMapping("/api/automation")
@CrossOrigin(origins = "*")
public class AutomationController {

    @Autowired
    private MLProductScraper scraper; // Serviço de Web Scraping

    @Autowired
    private MLAutomationService automationService; // Serviço de Simulação de IA

    /**
     * Endpoint: /analyze-ml
     * Realiza a análise completa de um produto.
     * 
     * O que este método faz:
     * 1. Chama o Scraper para pegar dados reais (Imagens, Título, Preço) do ML.
     * 2. Chama a IA (AutomationService) para injetar o status de Risco e imagens
     * CURADORAS.
     * 3. Prioriza as imagens enviadas pelo usuário (Locais) antes das imagens do
     * Scraper.
     */
    @GetMapping("/analyze-ml")
    public MLAnalysisResult analyze(@RequestParam String description) {
        // Busca dados reais no Mercado Livre
        MLAnalysisResult realResult = scraper.analyze(description);

        // Processa a inteligência de IA (Risco e Imagens Locais)
        AutomationResult aiResult = automationService.analisarProduto(null, description);

        // Injeta o status de risco (ALTO RISCO / BAIXO RISCO) no resultado final
        realResult.setStatus(aiResult.getStatus());

        // INTEGRAÇÃO DE IMAGENS:
        // Pegamos as imagens da IA (que são as locais org.webp, etc)
        // e as colocamos no INÍCIO da lista para que o funcionário veja primeiro.
        if (aiResult.getImageUrls() != null && !aiResult.getImageUrls().isEmpty()) {
            for (int i = aiResult.getImageUrls().size() - 1; i >= 0; i--) {
                String img = aiResult.getImageUrls().get(i);
                // Evita duplicados e insere no topo (index 0)
                if (!realResult.getImageUrls().contains(img)) {
                    realResult.getImageUrls().add(0, img);
                }
            }
        }

        return realResult;
    }

    /**
     * Endpoint: /search-ml
     * Retorna uma lista de produtos sugeridos para fins de pesquisa rápida.
     */
    @GetMapping("/search-ml")
    public List<MLProduct> search(@RequestParam String query) {
        return scraper.search(query);
    }
}
