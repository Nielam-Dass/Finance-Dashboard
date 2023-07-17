package com.dass.niel.finance.dashboard.marketperformancesnapshot;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MarketPerformanceSnapshot {
    int snapshotId;

    Map<String, Double> prices;

    LocalDate snapshotDate;

    public MarketPerformanceSnapshot(int snapshotId, Map<String, Double> prices, LocalDate snapshotDate) {
        this.snapshotId = snapshotId;
        this.prices = new HashMap<>(prices);
        this.snapshotDate = snapshotDate;
    }

    public MarketPerformanceSnapshot(Map<String, Double> prices, LocalDate snapshotDate) {
        this.snapshotId = -1;
        this.prices = new HashMap<>(prices);
        this.snapshotDate = snapshotDate;
    }

    public int getSnapshotId() {
        return snapshotId;
    }

    public Double getPriceOf(String ticker){
        if(prices.containsKey(ticker))
            return prices.get(ticker);
        else
            throw new IllegalArgumentException("Ticker symbol not found in this snapshot");
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }
}
