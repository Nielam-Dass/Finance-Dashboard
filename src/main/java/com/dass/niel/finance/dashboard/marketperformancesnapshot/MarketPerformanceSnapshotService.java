package com.dass.niel.finance.dashboard.marketperformancesnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Service
public class MarketPerformanceSnapshotService {
    @Autowired
    DataSource dataSource;
    @Value("${ticker_list}")
    String[] tickerList;

    private static final Logger logger = LoggerFactory.getLogger(MarketPerformanceSnapshotService.class);

    public List<MarketPerformanceSnapshot> getRecentSnapshots(){
        List<MarketPerformanceSnapshot> recentSnapshots = new ArrayList<>();
        String query = "SELECT * FROM market_performance_snapshot ORDER BY snapshot_date DESC LIMIT 2;";
        try {
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            String[] columnNames = getSqlColumnNames();
            while(resultSet.next()){
                int snapshotId = resultSet.getInt("snapshot_id");
                Map<String, Double> prices = new HashMap<>();
                for(int i=0; i<tickerList.length; i++){
                    prices.put(tickerList[i], resultSet.getDouble(columnNames[i]));
                }
                LocalDate snapshotDate = resultSet.getDate("snapshot_date").toLocalDate();
                recentSnapshots.add(new MarketPerformanceSnapshot(snapshotId, prices, snapshotDate));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        logger.info("Found {} recent snapshots.", recentSnapshots.size());
        return recentSnapshots;
    }

    public void addSnapshot(Map<String, Double> prices, LocalDate snapshotDate){
        String insertionCommandTemplate = "INSERT INTO market_performance_snapshot(%s) VALUES (%s);";
        String insertionCommand = String.format(insertionCommandTemplate, String.join(",", getSqlColumnNames()),
                ",?".repeat(tickerList.length+1).substring(1));

        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(insertionCommand);
            for(int i=0; i<tickerList.length; i++){
                preparedStatement.setDouble(i+1, prices.get(tickerList[i]));
            }
            preparedStatement.setDate(tickerList.length+1, Date.valueOf(snapshotDate));

            preparedStatement.executeUpdate();
            logger.info("1 row inserted into table market_performance_snapshot");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private String[] getSqlColumnNames(){
        String[] columnNames = new String[tickerList.length+1];
        for(int i=0; i<tickerList.length; i++) {
            columnNames[i] = tickerList[i].toLowerCase() + "_closing_price";
        }
        columnNames[columnNames.length-1] = "snapshot_date";
        return columnNames;
    }

    public SortedMap<LocalDate, Double> getPricesForTickerBetween(String tickerSymbol, LocalDate fromDate, LocalDate toDate){
        if(!Arrays.asList(tickerList).contains(tickerSymbol)){
            throw new IllegalArgumentException("Unknown ticker symbol: " + tickerSymbol);
        }
        SortedMap<LocalDate, Double> historicalPriceData = new TreeMap<>();
        String queryTemplate = "SELECT snapshot_date, %s FROM market_performance_snapshot WHERE snapshot_date>=? AND snapshot_date<=?;";
        String tickerColumnName = tickerSymbol.toLowerCase()+"_closing_price";
        String query = String.format(queryTemplate, tickerColumnName);

        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            Date fromSqlDate = (fromDate!=null) ? Date.valueOf(fromDate) : Date.valueOf("1000-01-01");
            Date toSqlDate = (toDate!=null) ? Date.valueOf(toDate) : Date.valueOf("9999-12-31");
            preparedStatement.setDate(1, fromSqlDate);
            preparedStatement.setDate(2, toSqlDate);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                LocalDate snapshotDate = resultSet.getDate("snapshot_date").toLocalDate();
                Double tickerSnapshotPrice = resultSet.getDouble(tickerColumnName);
                historicalPriceData.put(snapshotDate, tickerSnapshotPrice);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        logger.info("Found {} {} price data points.", historicalPriceData.size(), tickerSymbol);
        return historicalPriceData;
    }

}
