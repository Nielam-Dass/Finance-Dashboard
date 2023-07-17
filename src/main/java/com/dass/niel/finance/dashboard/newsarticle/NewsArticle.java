package com.dass.niel.finance.dashboard.newsarticle;

import java.time.LocalDate;

public class NewsArticle {
    private int articleId;
    private String articleUrl;
    private String articleTitle;
    private String articleDesc;
    private String articleThumbnail;
    private LocalDate dateRetrieved;

    public NewsArticle(int articleId, String articleUrl, String articleTitle, String articleDesc,
                       String articleThumbnail, LocalDate dateRetrieved) {
        this.articleId = articleId;
        this.articleUrl = articleUrl;
        this.articleTitle = articleTitle;
        this.articleDesc = articleDesc;
        this.articleThumbnail = articleThumbnail;
        this.dateRetrieved = dateRetrieved;
    }

    public NewsArticle(String articleUrl, String articleTitle, String articleDesc, String articleThumbnail,
                       LocalDate dateRetrieved) {
        this.articleId = -1;
        this.articleUrl = articleUrl;
        this.articleTitle = articleTitle;
        this.articleDesc = articleDesc;
        this.articleThumbnail = articleThumbnail;
        this.dateRetrieved = dateRetrieved;
    }

    public int getArticleId() {
        return articleId;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public String getArticleDesc() {
        return articleDesc;
    }

    public String getArticleThumbnail() {
        return articleThumbnail;
    }

    public LocalDate getDateRetrieved() {
        return dateRetrieved;
    }
}
