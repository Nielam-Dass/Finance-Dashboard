# Finance-Dashboard
This Spring Boot project retrieves data about the economy every morning, and stores it in a MySQL database. 
This data is then dynamically displayed in the dashboard main page using Thymeleaf templates. 
Users can also search through the archive of news articles and historical price data.

## How to use
1. Clone the repository by running `git clone https://github.com/Nielam-Dass/Finance-Dashboard.git`
2. Install dependencies in Maven's `pom.xml`. 
3. Install MySQL on port 3306 and create a database called `finance_dashboard`.
4. Create tables `news_article` and `market_performance_snapshot`.
5. Create an `application-dev.properties` file and define `spring.datasource.username` and `spring.datasource.password`.
6. Go to [Rapid API][rapidapi] and get a private key. In `application-dev.properties`, set the value of `rapid_api_key` to your private key.
7. Run `FinanceDashboardApplication.java` and go to http://localhost:8080/dashboard.

[rapidapi]: https://rapidapi.com/
