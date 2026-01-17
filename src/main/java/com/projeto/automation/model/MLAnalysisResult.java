package com.projeto.automation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MLAnalysisResult {
    private List<String> imageUrls;
    private String productUrl;
    private String title;
    private String price;
    private String status;
}
