package conectaseguros.co.news_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.NotNull;
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

  static void main(String[] args) {

    String activeProfile = System.getProperty("spring.profiles.active", "default");
    if ("dev".equals(activeProfile) || isLocalEnvironment()) {
      Dotenv dotenv = Dotenv.configure().directory("./news-service").ignoreIfMissing().load();

      setIfPresent("DB_USERNAME_DEV", dotenv);
      setIfPresent("DB_PASSWORD_DEV", dotenv);
    }

    SpringApplication.run(NewsServiceApplication.class, args);
  }

  private static void setIfPresent(String key, @NotNull Dotenv dotenv) {
    String value = dotenv.get(key);
    if (value != null) {
      System.setProperty(key, value);
    }
  }

  private static boolean isLocalEnvironment() {
    String env = System.getenv("ENVIRONMENT");
    return env == null || env.equals("local") || env.equals("development");
  }
}
