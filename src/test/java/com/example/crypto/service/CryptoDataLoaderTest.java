package com.example.crypto.service;

import com.example.crypto.exception.CryptoDataProcessingException;
import com.example.crypto.model.CryptoPrice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CryptoDataLoaderTest {

    @InjectMocks
    private CryptoDataLoader dataLoader;


    @Test
    public void testExtractSymbolFromFileName() {
        String fileName = "BTC_values.csv";
        String symbol = dataLoader.extractSymbolFromFileName(fileName);
        assertEquals("BTC", symbol);
    }

    @Test
    public void testLoadCryptoData() throws Exception {
        String csvContent = "timestamp,symbol,price\n" +
                "1641009600000,BTC,46813.21\n" +
                "1641020400000,BTC,46979.61\n" +
                "1641031200000,BTC,47143.98\n" +
                "1641034800000,BTC,46871.09\n" +
                "1641045600000,BTC,47023.24\n";

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        List<CryptoPrice> prices = dataLoader.loadCryptoData("BTC", inputStream);
        assertNotNull(prices);
        assertEquals(5, prices.size());

        CryptoPrice firstPrice = prices.get(0);
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(1641009600000L), ZoneId.systemDefault()), firstPrice.getTimestamp());
        assertEquals("BTC", firstPrice.getSymbol());
        assertEquals(46813.21, firstPrice.getPrice());
    }

    @Test
    public void testLoadCryptoData_InvalidCSV() throws Exception {
        String csvContent = "invalid,csv,content\n" +
                "not,a,number\n";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        // Assert that CryptoDataProcessingException is thrown
        assertThrows(CryptoDataProcessingException.class, () -> {
            dataLoader.loadCryptoData("BTC", inputStream);
        });
    }

    @Test
    public void testFileLoadingException() {

        Exception exception = assertThrows(CryptoDataProcessingException.class, () -> {
            dataLoader.scanCryptoDirectory();
        });

        assertEquals("Failed to scan crypto directory: null", exception.getMessage());
    }

    @Test
    public void testCryptoDataProcessingException() {
        // Act & Assert: Since the exception should happen inside the method
        Exception exception = assertThrows(CryptoDataProcessingException.class, () -> {
            dataLoader.scanCryptoDirectory();  // Directly call the method
        });

        assertTrue(exception.getMessage().contains("Failed to scan crypto directory"));
    }


}
