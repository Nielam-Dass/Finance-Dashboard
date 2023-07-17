package com.dass.niel.finance.dashboard;

import com.dass.niel.finance.dashboard.marketperformancesnapshot.MarketPerformanceSnapshotService;
import com.dass.niel.finance.dashboard.newsarticle.NewsArticle;
import com.dass.niel.finance.dashboard.newsarticle.NewsArticleService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@EnableScheduling
public class FinanceDataRetriever {
    @Autowired NewsArticleService newsArticleService;
    @Autowired MarketPerformanceSnapshotService marketPerformanceSnapshotService;
    @Autowired RestTemplate restTemplate;
    @Value("${rapid_api_key}") String rapidApiKey;
    @Value("${rapid_api_bingnews_host}") String rapidApiBingNewsHost;
    @Value("${bingnews_data_url}") String newsDataUrl;
    @Value("${rapid_api_stocks_host}") String rapidApiStocksHost;
    @Value("${apistocks_data_url}") String pricesDataUrl;
    @Value("${ticker_list}") String[] tickerList;

    private static final Logger logger = LoggerFactory.getLogger(FinanceDataRetriever.class);

    @Scheduled(cron = "0 30 9 * * *", zone = "America/New_York")
    public void retrieveNews(){
        logger.info("Retrieving news data");
        LocalDate currentDate = ZonedDateTime.now(ZoneId.of("America/New_York")).toLocalDate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", rapidApiKey);
        headers.set("X-RapidAPI-Host", rapidApiBingNewsHost);
        headers.set("X-BingApis-SDK", "true");
        HttpEntity<String> entity = new HttpEntity<>("", headers);
        ResponseEntity<String> re = restTemplate.exchange(newsDataUrl, HttpMethod.GET, entity, String.class);
        String jsonText = re.getBody();
        JSONObject respObj = new JSONObject(jsonText);
        JSONArray articles = respObj.getJSONArray("value");

        List<NewsArticle> newsArticles = new ArrayList<>();
        for(int i=0; i<articles.length(); i++){
            String articleTitle = articles.getJSONObject(i).getString("name");
            String articleUrl = articles.getJSONObject(i).getString("url");
            String articleDesc = articles.getJSONObject(i).getString("description");
            String imgUrl = "";
            if(articles.getJSONObject(i).has("image")){
                imgUrl = articles.getJSONObject(i).getJSONObject("image").getJSONObject("thumbnail").
                        getString("contentUrl");
            }
            newsArticles.add(new NewsArticle(articleUrl, articleTitle, articleDesc, imgUrl, currentDate));
        }
        newsArticleService.addArticles(newsArticles);
        logger.info("Completed retrieval of {} news articles", newsArticles.size());
    }

    @Scheduled(cron = "0 25 9 * * *", zone = "America/New_York")
    public void retrievePrices(){
        logger.info("Retrieving price data");
        LocalDate currentDate = ZonedDateTime.now(ZoneId.of("America/New_York")).toLocalDate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", rapidApiKey);
        headers.set("X-RapidAPI-Host", rapidApiStocksHost);
        HttpEntity<String> entity = new HttpEntity<>("", headers);
        Map<String, String> params = new HashMap<>();
        params.put("endDate", currentDate.toString());
        params.put("startDate", currentDate.minusDays(7).toString());

        Map<String, Double> prices = new HashMap<>();
        for(String tickerSymbol : tickerList){
            params.put("tickerSymbol", tickerSymbol);
            ResponseEntity<String> re = restTemplate.exchange(pricesDataUrl, HttpMethod.GET, entity, String.class, params);
            String jsonText = re.getBody();
            JSONObject respObj = new JSONObject(jsonText);
            JSONArray dailyPrices = respObj.getJSONArray("Results");
            Double recentPrice = dailyPrices.getJSONObject(dailyPrices.length()-1).getDouble("Close");
            prices.put(tickerSymbol, recentPrice);
        }
        marketPerformanceSnapshotService.addSnapshot(prices, currentDate);
        logger.info("Completed retrieval of price data");
    }

}
