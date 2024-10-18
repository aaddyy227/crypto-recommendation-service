package com.example.crypto.service;

import com.example.crypto.exception.CryptoNotFoundException;
import com.example.crypto.exception.NoCryptoDataAvailableException;
import com.example.crypto.model.CryptoPrice;
import com.example.crypto.model.CryptoStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CryptoService {

    private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);

    private final CryptoDataLoader dataLoader;

    public CryptoService(CryptoDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    public CryptoStatistics calculateStatistics(String symbol) {
        logger.info("Calculating statistics for crypto symbol: {}", symbol);
        List<CryptoPrice> prices = dataLoader.getCryptoData().get(symbol.toUpperCase());
        if (prices == null || prices.isEmpty()) {
            logger.warn("No data available for crypto symbol: {}", symbol);
            throw new CryptoNotFoundException("Unsupported or unavailable crypto: " + symbol);
        }

        // Ensure none of the timestamps or prices are null before sorting
        prices.removeIf(price -> price.getTimestamp() == null || price.getPrice() == 0); // Assuming 0 price is invalid, adjust as necessary

        if (prices.isEmpty()) {
            logger.warn("No valid data found after filtering for crypto: {}", symbol);
            throw new CryptoNotFoundException("No valid data available for crypto: " + symbol);
        }

        // Sorting by timestamp
        prices.sort(Comparator.comparing(CryptoPrice::getTimestamp));

        double oldestPrice = prices.get(0).getPrice();
        double newestPrice = prices.get(prices.size() - 1).getPrice();
        double minPrice = prices.stream().mapToDouble(CryptoPrice::getPrice).min().orElse(0);
        double maxPrice = prices.stream().mapToDouble(CryptoPrice::getPrice).max().orElse(0);
        double normalizedRange = (maxPrice - minPrice) / minPrice;

        logger.info("Statistics calculated for crypto symbol: {} - Oldest Price: {}, Newest Price: {}, Min Price: {}, Max Price: {}, Normalized Range: {}",
                symbol, oldestPrice, newestPrice, minPrice, maxPrice, normalizedRange);

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
        logger.info("Calculating statistics for all available cryptocurrencies");
        List<CryptoStatistics> statsList = new ArrayList<>();
        for (String symbol : dataLoader.getCryptoData().keySet()) {
            statsList.add(calculateStatistics(symbol));
        }
        statsList.sort(Comparator.comparing(CryptoStatistics::getNormalizedRange).reversed());
        logger.info("Statistics for all cryptocurrencies have been calculated and sorted");
        return statsList;
    }

    public String getHighestNormalizedCrypto(LocalDate date) {
        logger.info("Getting highest normalized range crypto for date: {}", date);
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
            logger.warn("No data available for the given date: {}", date);
            throw new NoCryptoDataAvailableException("No data available for the given date");
        }

        logger.info("Highest normalized range crypto for date {} is: {}", date, highestCrypto);
        return highestCrypto;
    }
}
