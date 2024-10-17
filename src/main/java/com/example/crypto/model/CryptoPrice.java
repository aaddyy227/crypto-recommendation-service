package com.example.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CryptoPrice {
    private LocalDateTime timestamp;
    private String symbol;
    private double price;

}
