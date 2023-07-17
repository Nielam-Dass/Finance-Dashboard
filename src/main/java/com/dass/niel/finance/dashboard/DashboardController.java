package com.dass.niel.finance.dashboard;

import com.dass.niel.finance.dashboard.marketperformancesnapshot.MarketPerformanceSnapshot;
import com.dass.niel.finance.dashboard.marketperformancesnapshot.MarketPerformanceSnapshotService;
import com.dass.niel.finance.dashboard.newsarticle.NewsArticle;
import com.dass.niel.finance.dashboard.newsarticle.NewsArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

@Controller
public class DashboardController {
    @Autowired
    NewsArticleService newsArticleService;

    @Autowired
    MarketPerformanceSnapshotService marketPerformanceSnapshotService;

    @Value("${ticker_list}")
    String[] tickerList;

    @GetMapping("/dashboard")
    public String viewDashboard(Model model){
        List<NewsArticle> recentArticles = newsArticleService.getRecentArticles();
        List<MarketPerformanceSnapshot> recentMarketSnapshots = marketPerformanceSnapshotService.getRecentSnapshots();
        model.addAttribute("recentArticles", recentArticles);
        model.addAttribute("recentMarketSnapshots", recentMarketSnapshots);
        model.addAttribute("tickerList", tickerList);
        return "dashboard";
    }

    @GetMapping("/search/price-data")
    public String viewPriceDataSearch(@RequestParam(required = false) String tickerSelection,
                                      @RequestParam(required = false) String fromDate,
                                      @RequestParam(required = false) String toDate,
                                      Model model){
        model.addAttribute("tickerList", tickerList);
        if(fromDate!=null && toDate!=null && tickerSelection!=null){
            LocalDate fromLocalDate = (!fromDate.equals("")) ? LocalDate.parse(fromDate) : null;
            LocalDate toLocalDate = (!toDate.equals("")) ? LocalDate.parse(toDate) : null;
            SortedMap<LocalDate, Double> historicalPriceData = marketPerformanceSnapshotService.getPricesForTickerBetween(tickerSelection, fromLocalDate, toLocalDate);
            if(!historicalPriceData.isEmpty()){
                LocalDate firstDate = historicalPriceData.firstKey();
                LocalDate lastDate = historicalPriceData.lastKey();
                if(Period.between(firstDate, lastDate).getDays()+1 > historicalPriceData.size()){
                    throw new RuntimeException("Missing price values between " + firstDate + " and " + lastDate);
                }
                List<Double> prices = new ArrayList<>(historicalPriceData.values());

                model.addAttribute("prices", prices);
                model.addAttribute("firstDate", firstDate);
                model.addAttribute("lastDate", lastDate);
            }
            else{
                model.addAttribute("noDataFound", true);
            }
            model.addAttribute("tickerSelection", tickerSelection);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);

        }
        else{
            model.addAttribute("fromDate", "");
            model.addAttribute("toDate", "");
            model.addAttribute("tickerSymbol", "");
        }

        return "search_price_data";
    }

    @GetMapping("/search/news-articles")
    public String viewNewsArticleSearch(@RequestParam(required = false) String fromDate,
                                        @RequestParam(required = false) String toDate,
                                        @RequestParam(required = false) String q,
                                        Model model){
        if(fromDate!=null && toDate!=null && q!=null){
            LocalDate fromLocalDate = (!fromDate.equals("")) ? LocalDate.parse(fromDate) : null;
            LocalDate toLocalDate = (!toDate.equals("")) ? LocalDate.parse(toDate) : null;
            List<NewsArticle> filteredArticles = newsArticleService.filterArticles(fromLocalDate, toLocalDate, q);
            model.addAttribute("filteredArticles", filteredArticles);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
            model.addAttribute("q", q);
        }
        else{
            model.addAttribute("fromDate", "");
            model.addAttribute("toDate", "");
            model.addAttribute("q", "");
        }

        return "search_news_articles";
    }
}
