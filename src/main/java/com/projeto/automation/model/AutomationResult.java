package com.projeto.automation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutomationResult {
    private List<String> imageUrls;
    private String status;
}
