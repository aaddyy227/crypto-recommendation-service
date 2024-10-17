package com.example.crypto.service;

import com.example.crypto.exception.CryptoNotFoundException;
import com.example.crypto.exception.NoCryptoDataAvailableException;
import com.example.crypto.model.CryptoPrice;
import com.example.crypto.model.CryptoStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CryptoServiceTest {

    @InjectMocks
    private CryptoService cryptoService;

    @Mock
    private CryptoDataLoader dataLoader;

    private Map<String, List<CryptoPrice>> cryptoData;

    @BeforeEach
    public void setUp() {
        cryptoData = new HashMap<>();

        // Prepare mock data
        List<CryptoPrice> btcPrices = Arrays.asList(
                new CryptoPrice(LocalDateTime.of(2022, 1, 1, 0, 0), "BTC", 46813.21),
                new CryptoPrice(LocalDateTime.of(2022, 1, 1, 3, 0), "BTC", 46979.61),
                new CryptoPrice(LocalDateTime.of(2022, 1, 1, 6, 0), "BTC", 47143.98),
                new CryptoPrice(LocalDateTime.of(2022, 1, 1, 9, 0), "BTC", 46871.09),
                new CryptoPrice(LocalDateTime.of(2022, 1, 1, 12, 0), "BTC", 47023.24)
        );

        List<CryptoPrice> ethPrices = Arrays.asList(
                new CryptoPrice(LocalDateTime.of(2022, 1, 1, 0, 0), "ETH", 3681.1),
                new CryptoPrice(LocalDateTime.of(2022, 1, 1, 3, 0), "ETH", 3690.5),
                new CryptoPrice(LocalDateTime.of(2022, 1, 1, 6, 0), "ETH", 3700.2),
                new CryptoPrice(LocalDateTime.of(2022, 1, 1, 9, 0), "ETH", 3685.8),
                new CryptoPrice(LocalDateTime.of(2022, 1, 1, 12, 0), "ETH", 3695.0)
        );

        cryptoData.put("BTC", btcPrices);
        cryptoData.put("ETH", ethPrices);

        when(dataLoader.getCryptoData()).thenReturn(cryptoData);
    }

    @Test
    public void testCalculateStatistics_ValidSymbol() {
        CryptoStatistics stats = cryptoService.calculateStatistics("BTC");
        assertNotNull(stats);
        assertEquals("BTC", stats.getSymbol());
        assertEquals(46813.21, stats.getOldestPrice());
        assertEquals(47023.24, stats.getNewestPrice());
        assertEquals(46813.21, stats.getMinPrice());
        assertEquals(47143.98, stats.getMaxPrice());
        double expectedNormalizedRange = (47143.98 - 46813.21) / 46813.21;
        assertEquals(expectedNormalizedRange, stats.getNormalizedRange());
    }

    @Test
    public void testCalculateStatistics_InvalidSymbol() {
        Exception exception = assertThrows(CryptoNotFoundException.class, () -> {
            cryptoService.calculateStatistics("DOGE");
        });
        assertEquals("Unsupported or unavailable crypto: DOGE", exception.getMessage());
    }

    @Test
    public void testCalculateStatistics_NoValidData() {
        // Mock a symbol with empty price list
        cryptoData.put("XRP", Collections.emptyList());

        Exception exception = assertThrows(CryptoNotFoundException.class, () -> {
            cryptoService.calculateStatistics("XRPS");
        });
        assertEquals("Unsupported or unavailable crypto: XRPS", exception.getMessage());
    }

    @Test
    public void testGetAllCryptoStatistics() {
        List<CryptoStatistics> statsList = cryptoService.getAllCryptoStatistics();
        assertNotNull(statsList);
        assertEquals(2, statsList.size());
        // Since we have only BTC and ETH, check that both are present
        Set<String> symbols = new HashSet<>();
        for (CryptoStatistics stats : statsList) {
            symbols.add(stats.getSymbol());
        }
        assertTrue(symbols.contains("BTC"));
        assertTrue(symbols.contains("ETH"));
    }

    @Test
    public void testGetHighestNormalizedCrypto_ValidDate() {
        LocalDate date = LocalDate.of(2022, 1, 1);
        String highestCrypto = cryptoService.getHighestNormalizedCrypto(date);
        assertNotNull(highestCrypto);
        // Determine which of BTC or ETH has higher normalized range on the given date
        // Let's calculate normalized ranges
        List<CryptoPrice> btcPrices = cryptoData.get("BTC");
        List<CryptoPrice> btcFilteredPrices = new ArrayList<>();
        for (CryptoPrice price : btcPrices) {
            if (price.getTimestamp().toLocalDate().equals(date)) {
                btcFilteredPrices.add(price);
            }
        }
        double btcMinPrice = btcFilteredPrices.stream().mapToDouble(CryptoPrice::getPrice).min().orElse(0);
        double btcMaxPrice = btcFilteredPrices.stream().mapToDouble(CryptoPrice::getPrice).max().orElse(0);
        double btcNormalizedRange = (btcMaxPrice - btcMinPrice) / btcMinPrice;

        List<CryptoPrice> ethPrices = cryptoData.get("ETH");
        List<CryptoPrice> ethFilteredPrices = new ArrayList<>();
        for (CryptoPrice price : ethPrices) {
            if (price.getTimestamp().toLocalDate().equals(date)) {
                ethFilteredPrices.add(price);
            }
        }
        double ethMinPrice = ethFilteredPrices.stream().mapToDouble(CryptoPrice::getPrice).min().orElse(0);
        double ethMaxPrice = ethFilteredPrices.stream().mapToDouble(CryptoPrice::getPrice).max().orElse(0);
        double ethNormalizedRange = (ethMaxPrice - ethMinPrice) / ethMinPrice;

        if (btcNormalizedRange > ethNormalizedRange) {
            assertEquals("BTC", highestCrypto);
        } else {
            assertEquals("ETH", highestCrypto);
        }
    }

    @Test
    public void testGetHighestNormalizedCrypto_NoDataForDate() {
        LocalDate date = LocalDate.of(2021, 12, 31);
        Exception exception = assertThrows(NoCryptoDataAvailableException.class, () -> {
            cryptoService.getHighestNormalizedCrypto(date);
        });
        assertEquals("No data available for the given date", exception.getMessage());
    }
}
