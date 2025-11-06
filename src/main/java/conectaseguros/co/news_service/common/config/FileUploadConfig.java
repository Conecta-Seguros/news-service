package conectaseguros.co.news_service.common.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
public class FileUploadConfig {

  @Bean
  public MultipartConfigElement multipartConfigElement() {
    MultipartConfigFactory factory = new MultipartConfigFactory();

    // Configurar tamaños máximos
    factory.setMaxFileSize(DataSize.ofMegabytes(50)); // 50MB por archivo
    factory.setMaxRequestSize(DataSize.ofMegabytes(60)); // 60MB por request

    // Directorio temporal
    factory.setLocation("/tmp");

    return factory.createMultipartConfig();
  }

  @Bean
  public MultipartResolver multipartResolver() {
    return new StandardServletMultipartResolver();
  }
}
