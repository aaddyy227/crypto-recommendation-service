package com.example.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoStatistics {
    private String symbol;
    private double oldestPrice;
    private double newestPrice;
    private double minPrice;
    private double maxPrice;
    private double normalizedRange;

}
