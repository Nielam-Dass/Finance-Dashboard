package com.dass.niel.finance.dashboard.newsarticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsArticleService {
    @Autowired
    DataSource dataSource;

    private static final Logger logger = LoggerFactory.getLogger(NewsArticleService.class);

    public List<NewsArticle> getRecentArticles(){
        List<NewsArticle> recentArticles = new ArrayList<>();
        String query = "SELECT * FROM news_article ORDER BY date_retrieved DESC LIMIT 10;";
        try {
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()){
                int articleId = resultSet.getInt("article_id");
                String articleUrl = resultSet.getString("article_url");
                String articleTitle = resultSet.getString("article_title");
                String articleDesc = resultSet.getString("article_desc");
                String articleThumbnail = resultSet.getString("article_thumbnail");
                LocalDate dateRetrieved = resultSet.getDate("date_retrieved").toLocalDate();
                recentArticles.add(new NewsArticle(articleId, articleUrl, articleTitle, articleDesc,
                        articleThumbnail, dateRetrieved));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        logger.info("Found {} recent articles.", recentArticles.size());
        return recentArticles;
    }

    public void addArticles(List<NewsArticle> newsArticles){
        String insertionCommand = "INSERT INTO news_article(article_url, article_title, article_desc, " +
                "article_thumbnail, date_retrieved) values (?,?,?,?,?);";

        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(insertionCommand);
            int rowsInserted = 0;
            for(NewsArticle newsArticle : newsArticles){
                preparedStatement.setString(1, newsArticle.getArticleUrl());
                preparedStatement.setString(2, newsArticle.getArticleTitle());
                preparedStatement.setString(3, newsArticle.getArticleDesc());
                preparedStatement.setString(4, newsArticle.getArticleThumbnail());
                preparedStatement.setDate(5, Date.valueOf(newsArticle.getDateRetrieved()));
                rowsInserted += preparedStatement.executeUpdate();
            }
            logger.info("{} row(s) inserted into table news_article", rowsInserted);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<NewsArticle> filterArticles(LocalDate fromDate, LocalDate toDate, String q){
        List<NewsArticle> filteredArticles = new ArrayList<>();
        String query = "SELECT * FROM news_article WHERE date_retrieved>=? AND date_retrieved<=? " +
                "AND (article_title LIKE ? OR article_desc LIKE ?) ORDER BY date_retrieved DESC;";
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            Date fromSqlDate = (fromDate!=null) ? Date.valueOf(fromDate) : Date.valueOf("1000-01-01");
            Date toSqlDate = (toDate!=null) ? Date.valueOf(toDate) : Date.valueOf("9999-12-31");
            preparedStatement.setDate(1, fromSqlDate);
            preparedStatement.setDate(2, toSqlDate);
            preparedStatement.setString(3, "%"+q+"%");
            preparedStatement.setString(4, "%"+q+"%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                int articleId = resultSet.getInt("article_id");
                String articleUrl = resultSet.getString("article_url");
                String articleTitle = resultSet.getString("article_title");
                String articleDesc = resultSet.getString("article_desc");
                String articleThumbnail = resultSet.getString("article_thumbnail");
                LocalDate dateRetrieved = resultSet.getDate("date_retrieved").toLocalDate();
                filteredArticles.add(new NewsArticle(articleId, articleUrl, articleTitle, articleDesc,
                        articleThumbnail, dateRetrieved));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        logger.info("Found {} filtered articles.", filteredArticles.size());
        return filteredArticles;
    }

}
