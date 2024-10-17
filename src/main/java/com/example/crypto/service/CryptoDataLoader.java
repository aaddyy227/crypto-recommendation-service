package com.example.crypto.service;

import com.example.crypto.exception.CryptoDataProcessingException;
import com.example.crypto.exception.FileLoadingException;
import com.example.crypto.model.CryptoPrice;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CryptoDataLoader {

    @Getter
    private final Map<String, List<CryptoPrice>> cryptoData = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(CryptoDataLoader.class);


    @Value("${crypto.directory.path}")
    private String cryptoFolderPath;  // Configurable path to the crypto directory

    @PostConstruct
    public void init() {
        scanCryptoDirectory();  // Initial scan on startup
    }

    @Scheduled(fixedDelayString = "${crypto.scan.interval}")
    public void scanCryptoDirectory() {
        try {
            // Directly access the external directory from the file system
            File cryptoFolder = new File(cryptoFolderPath);

            if (!cryptoFolder.exists() || !cryptoFolder.isDirectory()) {
                throw new FileLoadingException("Crypto directory does not exist or is not a directory: " + cryptoFolderPath);
            }

            for (File file : Objects.requireNonNull(cryptoFolder.listFiles((dir, name) -> name.endsWith("_values.csv")))) {
                String fileName = file.getName();
                String cryptoSymbol = extractSymbolFromFileName(fileName);

                if (!cryptoData.containsKey(cryptoSymbol)) {
                    try (InputStream inputStream = new FileInputStream(file)) {
                        List<CryptoPrice> prices = loadCryptoData(cryptoSymbol, inputStream);
                        cryptoData.put(cryptoSymbol, prices);
                    } catch (FileNotFoundException e) {
                        throw new FileLoadingException("File not found: " + fileName);
                    }
                }
            }
        } catch (Exception e) {
            throw new CryptoDataProcessingException("Failed to scan crypto directory: " + cryptoFolderPath, e);
        }
    }


    public String extractSymbolFromFileName(String fileName) {
        return fileName.split("_")[0];
    }

    public List<CryptoPrice> loadCryptoData(String symbol, InputStream inputStream) {
        List<CryptoPrice> prices = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] line;
            reader.readNext(); // Skip header
            while ((line = reader.readNext()) != null) {
                long timestamp = Long.parseLong(line[0]);
                double price = Double.parseDouble(line[2]);
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp),
                        ZoneId.systemDefault());
                prices.add(new CryptoPrice(dateTime, symbol, price));
            }
        } catch (Exception e) {
            throw new CryptoDataProcessingException("Error processing crypto data for symbol: " + symbol, e);
        }
        return prices;
    }
}
