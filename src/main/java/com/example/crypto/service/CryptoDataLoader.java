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
        logger.info("Initializing CryptoDataLoader...");
        scanCryptoDirectory();  // Initial scan on startup
    }

    @Scheduled(fixedDelayString = "${crypto.scan.interval}")
    public void scanCryptoDirectory() {
        logger.info("Scanning crypto directory at path: {}", cryptoFolderPath);

        try {
            File cryptoFolder = new File(cryptoFolderPath);

            if (!cryptoFolder.exists() || !cryptoFolder.isDirectory()) {
                logger.error("Crypto directory does not exist or is not a directory: {}", cryptoFolderPath);
                throw new FileLoadingException("Crypto directory does not exist or is not a directory: " + cryptoFolderPath);
            }

            for (File file : Objects.requireNonNull(cryptoFolder.listFiles((dir, name) -> name.endsWith("_values.csv")))) {
                String fileName = file.getName();
                String cryptoSymbol = extractSymbolFromFileName(fileName);

                if (!cryptoData.containsKey(cryptoSymbol)) {
                    logger.info("Loading data for crypto symbol: {}", cryptoSymbol);
                    try (InputStream inputStream = new FileInputStream(file)) {
                        List<CryptoPrice> prices = loadCryptoData(cryptoSymbol, inputStream);
                        cryptoData.put(cryptoSymbol, prices);
                        logger.info("Successfully loaded {} price records for crypto symbol: {}", prices.size(), cryptoSymbol);
                    } catch (FileNotFoundException e) {
                        logger.error("File not found: {}", fileName, e);
                        throw new FileLoadingException("File not found: " + fileName);
                    }
                } else {
                    logger.info("Data for crypto symbol: {} already loaded, skipping file: {}", cryptoSymbol, fileName);
                }
            }

        } catch (Exception e) {
            logger.error("Error occurred while scanning crypto directory: {}", cryptoFolderPath, e);
            throw new CryptoDataProcessingException("Failed to scan crypto directory: " + cryptoFolderPath, e);
        }
    }

    public String extractSymbolFromFileName(String fileName) {
        String symbol = fileName.split("_")[0];
        logger.debug("Extracted crypto symbol: {} from file name: {}", symbol, fileName);
        return symbol;
    }

    public List<CryptoPrice> loadCryptoData(String symbol, InputStream inputStream) {
        List<CryptoPrice> prices = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            logger.debug("Reading CSV data for symbol: {}", symbol);
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
            logger.error("Error processing crypto data for symbol: {}", symbol, e);
            throw new CryptoDataProcessingException("Error processing crypto data for symbol: " + symbol, e);
        }
        logger.debug("Loaded {} records for crypto symbol: {}", prices.size(), symbol);
        return prices;
    }
}
