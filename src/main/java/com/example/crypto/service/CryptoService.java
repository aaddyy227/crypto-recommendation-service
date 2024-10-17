package com.example.crypto.service;

import com.example.crypto.exception.CryptoNotFoundException;
import com.example.crypto.exception.NoCryptoDataAvailableException;
import com.example.crypto.model.CryptoPrice;
import com.example.crypto.model.CryptoStatistics;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CryptoService {

    private final CryptoDataLoader dataLoader;

    public CryptoService(CryptoDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    public CryptoStatistics calculateStatistics(String symbol) {
        List<CryptoPrice> prices = dataLoader.getCryptoData().get(symbol.toUpperCase());
        if (prices == null || prices.isEmpty()) {
            throw new CryptoNotFoundException("Unsupported or unavailable crypto: " + symbol);
        }

        // Ensure none of the timestamps or prices are null before sorting
        prices.removeIf(price -> price.getTimestamp() == null || price.getPrice() == 0); // Assuming 0 price is invalid, adjust as necessary

        if (prices.isEmpty()) {
            throw new CryptoNotFoundException("No valid data available for crypto: " + symbol);
        }

        // Sorting by timestamp
        prices.sort(Comparator.comparing(CryptoPrice::getTimestamp));

        double oldestPrice = prices.getFirst().getPrice();
        double newestPrice = prices.getLast().getPrice();
        double minPrice = prices.stream().mapToDouble(CryptoPrice::getPrice).min().orElse(0);
        double maxPrice = prices.stream().mapToDouble(CryptoPrice::getPrice).max().orElse(0);
        double normalizedRange = (maxPrice - minPrice) / minPrice;

        return new CryptoStatistics(
                symbol.toUpperCase(),
                oldestPrice,
                newestPrice,
                minPrice,
                maxPrice,
                normalizedRange
        );
    }

    public List<CryptoStatistics> getAllCryptoStatistics() {
        List<CryptoStatistics> statsList = new ArrayList<>();
        for (String symbol : dataLoader.getCryptoData().keySet()) {
            statsList.add(calculateStatistics(symbol));
        }
        statsList.sort(Comparator.comparing(CryptoStatistics::getNormalizedRange).reversed());
        return statsList;
    }

    public String getHighestNormalizedCrypto(LocalDate date) {
        String highestCrypto = null;
        double highestNormalizedRange = -1;

        for (String symbol : dataLoader.getCryptoData().keySet()) {
            List<CryptoPrice> prices = dataLoader.getCryptoData().get(symbol);
            List<CryptoPrice> filteredPrices = new ArrayList<>();

            for (CryptoPrice price : prices) {
                if (price.getTimestamp().toLocalDate().equals(date)) {
                    filteredPrices.add(price);
                }
            }

            if (filteredPrices.size() < 2) continue;

            double minPrice = filteredPrices.stream()
                    .mapToDouble(CryptoPrice::getPrice).min().orElse(0);
            double maxPrice = filteredPrices.stream()
                    .mapToDouble(CryptoPrice::getPrice).max().orElse(0);
            double normalizedRange = (maxPrice - minPrice) / minPrice;

            if (normalizedRange > highestNormalizedRange) {
                highestNormalizedRange = normalizedRange;
                highestCrypto = symbol;
            }
        }

        if (highestCrypto == null) {
            throw new NoCryptoDataAvailableException("No data available for the given date");
        }

        return highestCrypto;
    }
}
