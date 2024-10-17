package com.example.crypto.controller;

import com.example.crypto.model.CryptoStatistics;
import com.example.crypto.service.CryptoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Operation(summary = "Get cryptos sorted by normalized range",
            description = "Returns a descending sorted list of all cryptocurrencies, based on their normalized range (i.e., (max-min)/min).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of cryptocurrencies with their statistics"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/normalized")
    public List<CryptoStatistics> getAllCryptoStatistics() {
        return cryptoService.getAllCryptoStatistics();
    }

    @Operation(summary = "Get statistics for a specific crypto",
            description = "Returns the oldest, newest, minimum, and maximum prices for the specified cryptocurrency.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cryptocurrency statistics"),
            @ApiResponse(responseCode = "404", description = "Crypto not found"),
            @ApiResponse(responseCode = "400", description = "Invalid symbol format")
    })
    @GetMapping("/{symbol}/statistics")
    public CryptoStatistics getCryptoStatistics(
            @Parameter(description = "Symbol of the cryptocurrency (e.g., BTC, ETH)") @PathVariable String symbol) {
        return cryptoService.calculateStatistics(symbol);
    }

    @Operation(summary = "Get crypto with the highest normalized range for a specific day",
            description = "Returns the cryptocurrency with the highest normalized range for the provided date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cryptocurrency with the highest normalized range"),
            @ApiResponse(responseCode = "400", description = "Invalid date format"),
            @ApiResponse(responseCode = "404", description = "No crypto found for the provided date")
    })
    @GetMapping("/highest-normalized")
    public String getHighestNormalizedCrypto(
            @Parameter(description = "Date for which to find the highest normalized range", example = "2022-01-01")
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return cryptoService.getHighestNormalizedCrypto(date);
    }
}
