use finance_dashboard;

create table if not exists news_article(
	article_id int auto_increment,
    article_url varchar(512),
    article_title varchar(512),
    article_desc varchar(512),
    article_thumbnail varchar(512),
    date_retrieved date,
    primary key(article_id)
);

create table if not exists market_performance_snapshot(
	snapshot_id int auto_increment,
    djia_closing_price double,
    spy_closing_price double,
    oneq_closing_price double,
    snapshot_date date,
    primary key(snapshot_id)
);
