package conectaseguros.co.news_service;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Objects;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableConfigurationProperties
@EnableJpaRepositories
public class NewsServiceApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure()
				.directory("./news-service")
				.load();

		System.setProperty("DB_USERNAME", Objects.requireNonNull(dotenv.get("DB_USERNAME")));
		System.setProperty("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));

		SpringApplication.run(NewsServiceApplication.class, args);
	}

}
