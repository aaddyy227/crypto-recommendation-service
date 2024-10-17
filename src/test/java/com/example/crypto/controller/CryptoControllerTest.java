package com.example.crypto.controller;

import com.example.crypto.model.CryptoStatistics;
import com.example.crypto.service.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CryptoController.class)
public class CryptoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CryptoService cryptoService;

    private CryptoStatistics btcStatistics;
    private List<CryptoStatistics> statisticsList;

    @BeforeEach
    public void setUp() {
        // Prepare mock data
        btcStatistics = new CryptoStatistics(
                "BTC",
                46813.21,
                47023.24,
                46813.21,
                47143.98,
                (47143.98 - 46813.21) / 46813.21
        );

        CryptoStatistics ethStatistics = new CryptoStatistics(
                "ETH",
                3681.1,
                3695.0,
                3681.1,
                3700.2,
                (3700.2 - 3681.1) / 3681.1
        );

        statisticsList = Arrays.asList(btcStatistics, ethStatistics);
    }

    @Test
    public void testGetAllCryptoStatistics() throws Exception {
        when(cryptoService.getAllCryptoStatistics()).thenReturn(statisticsList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/crypto/normalized"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("BTC"))
                .andExpect(jsonPath("$[0].oldestPrice").value(46813.21))
                .andExpect(jsonPath("$[1].symbol").value("ETH"));

        verify(cryptoService, times(1)).getAllCryptoStatistics();
    }

    @Test
    public void testGetCryptoStatistics_ValidSymbol() throws Exception {
        when(cryptoService.calculateStatistics("BTC")).thenReturn(btcStatistics);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/crypto/BTC/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTC"))
                .andExpect(jsonPath("$.oldestPrice").value(46813.21))
                .andExpect(jsonPath("$.newestPrice").value(47023.24))
                .andExpect(jsonPath("$.minPrice").value(46813.21))
                .andExpect(jsonPath("$.maxPrice").value(47143.98))
                .andExpect(jsonPath("$.normalizedRange").value((47143.98 - 46813.21) / 46813.21));

        verify(cryptoService, times(1)).calculateStatistics("BTC");
    }

    @Test
    public void testGetCryptoStatistics_InvalidSymbol() throws Exception {
        when(cryptoService.calculateStatistics("DOGER")).thenThrow(new IllegalArgumentException("Unsupported or unavailable crypto: DOGER"));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/crypto/DOGER/statistics"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unsupported or unavailable crypto: DOGER"));

        verify(cryptoService, times(1)).calculateStatistics("DOGER");
    }

    @Test
    public void testGetHighestNormalizedCrypto_ValidDate() throws Exception {
        when(cryptoService.getHighestNormalizedCrypto(LocalDate.of(2022, 1, 1))).thenReturn("BTC");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/crypto/highest-normalized")
                        .param("date", "2022-01-01"))
                .andExpect(status().isOk())
                .andExpect(content().string("BTC"));

        verify(cryptoService, times(1)).getHighestNormalizedCrypto(LocalDate.of(2022, 1, 1));
    }

    @Test
    public void testGetHighestNormalizedCrypto_InvalidDate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/crypto/highest-normalized")
                        .param("date", "invalid-date"))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(cryptoService);
    }

}
