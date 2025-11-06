package conectaseguros.co.news_service.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {

  @Bean
  public Dotenv getDotenv() {
    return Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();
  }
}
