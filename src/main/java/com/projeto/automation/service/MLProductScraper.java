package com.projeto.automation.service;

import com.projeto.automation.model.MLAnalysisResult;
import com.projeto.automation.model.MLProduct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * WEB SCRAPER - MERCADO LIVRE (MLProductScraper.java)
 * 
 * Este serviço realiza a extração de dados reais do Mercado Livre usando Jsoup.
 * Ele contorna bloqueios 403 simulando um navegador real através de Headers
 * HTTP.
 */
@Service
public class MLProductScraper {

    // User Agent atualizado para evitar detecção de Bot
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * Analisa uma consulta (query) buscando o primeiro produto no ML
     * e extraindo sua galeria completa de imagens.
     */
    public MLAnalysisResult analyze(String query) {
        MLAnalysisResult result = new MLAnalysisResult();
        List<String> images = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return getFallbackMock(query);
        }

        try {
            // Encode dos termos de busca para a URL
            String searchUrl = "https://lista.mercadolivre.com.br/"
                    + URLEncoder.encode(query, StandardCharsets.UTF_8).replace("+", "-");
            Document doc = fetchDocument(searchUrl);

            // Seleção do primeiro item da lista de busca
            Element productElement = doc.selectFirst("li.ui-search-layout__item");
            if (productElement == null) {
                productElement = doc.selectFirst("div.ui-search-result__wrapper");
            }

            if (productElement != null) {
                // Extração dos dados básicos (Título, Preço, Link)
                Element titleEl = productElement.selectFirst("h2.ui-search-item__title");
                result.setTitle((titleEl != null) ? titleEl.text() : query);

                Element priceEl = productElement.selectFirst("span.andes-money-amount__fraction");
                result.setPrice((priceEl != null) ? priceEl.text() : "0.00");

                Element linkEl = productElement.selectFirst("a.ui-search-link");
                String productUrl = (linkEl != null) ? linkEl.attr("href") : null;
                result.setProductUrl(productUrl);

                // Thumbnail principal (aquela que aparece na busca)
                Element imgEl = productElement.selectFirst("img.ui-search-result-image__element");
                if (imgEl != null) {
                    String src = imgEl.attr("src");
                    if (src == null || src.isEmpty())
                        src = imgEl.attr("data-src");
                    if (src != null && !src.isEmpty())
                        images.add(src);
                }

                // ACESSO À PÁGINA DO PRODUTO: Busca galeria de alta definição
                if (productUrl != null && !productUrl.isEmpty()) {
                    try {
                        Document productDoc = fetchDocument(productUrl);
                        // Seletores específicos para a galeria de imagens do Mercado Livre
                        Elements galleryImgs = productDoc.select("figure.ui-pdp-gallery__figure img.ui-pdp-image");
                        if (galleryImgs.isEmpty())
                            galleryImgs = productDoc.select("img.ui-pdp-image");

                        for (Element img : galleryImgs) {
                            String src = img.attr("src");
                            if (src == null || src.isEmpty())
                                src = img.attr("data-src");

                            // Evita duplicados e limita a 6 imagens para performance
                            if (src != null && !src.isEmpty() && !images.contains(src)) {
                                images.add(src);
                            }
                            if (images.size() >= 6)
                                break;
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao extrair galeria detalhada: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Se não encontrar nada real, usa os dados simulados (Fallback)
        if (images.isEmpty())
            return getFallbackMock(query);

        result.setImageUrls(images);
        return result;
    }

    /**
     * Sistema de Busca secundário para preencher listas de pesquisa.
     */
    public List<MLProduct> search(String query) {
        List<MLProduct> products = new ArrayList<>();
        try {
            String searchUrl = "https://lista.mercadolivre.com.br/"
                    + URLEncoder.encode(query, StandardCharsets.UTF_8).replace("+", "-");
            Document doc = fetchDocument(searchUrl);

            Elements items = doc.select("li.ui-search-layout__item");
            if (items.isEmpty())
                items = doc.select("div.ui-search-result__wrapper");

            for (Element item : items) {
                if (products.size() >= 10)
                    break;

                Element titleEl = item.selectFirst("h2.ui-search-item__title");
                Element priceEl = item.selectFirst("span.andes-money-amount__fraction");
                Element linkEl = item.selectFirst("a.ui-search-link");
                Element imgEl = item.selectFirst("img.ui-search-result-image__element");

                String title = (titleEl != null) ? titleEl.text() : "Produto sem Título";
                String priceStr = (priceEl != null) ? priceEl.text().replace(".", "") : "0";
                BigDecimal price = new BigDecimal(priceStr);

                String url = (linkEl != null) ? linkEl.attr("href") : "";
                String img = (imgEl != null)
                        ? (imgEl.attr("src").isEmpty() ? imgEl.attr("data-src") : imgEl.attr("src"))
                        : "";

                products.add(new MLProduct("MLB" + UUID.randomUUID().toString().substring(0, 8), title, price, url, img,
                        "Mercado Livre", "Novo", "Grátis"));
            }
        } catch (Exception e) {
        }
        return products;
    }

    /**
     * Configura a conexão HTTP simulando um navegador Real.
     * Crucial para evitar o erro 403 Forbidden.
     */
    private Document fetchDocument(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "pt-BR,pt;q=0.9")
                .header("Referer", "https://www.google.com/")
                .timeout(10000)
                .get();
    }

    /**
     * Dados MOCK (Simulados) para garantir que o sistema sempre funcione
     * mesmo offline ou com erro de conexão no Scraper.
     */
    private MLAnalysisResult getFallbackMock(String query) {
        MLAnalysisResult result = new MLAnalysisResult();
        List<String> images = new ArrayList<>();
        String q = query != null ? query.toLowerCase() : "";

        if (q.contains("iphone")) {
            images.add("/img/iphone.webp");
            result.setTitle("iPhone 15 (Mock)");
        } else if (q.contains("samsung")) {
            images.add("/img/S25 ultra.webp");
            result.setTitle("Galaxy S25 (Mock)");
        } else if (q.contains("monitor")) {
            images.add("/img/monitor.webp");
            result.setTitle("Monitor Dell (Mock)");
        } else {
            images.add("/img/monitor3.webp");
            result.setTitle("Produto Geral");
        }

        result.setImageUrls(images);
        result.setPrice("0.00");
        result.setProductUrl("https://mercadolivre.com.br");
        return result;
    }
}
