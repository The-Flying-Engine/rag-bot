package dev.gov.rag_boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
  @Value("${app.cors.allowed-origins:http://localhost:5173}")
  private String allowedOrigins;

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.addAllowedOriginPattern(allowedOrigins);
    cfg.addAllowedMethod("*");
    cfg.addAllowedHeader("*");
    cfg.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/**", cfg);
    return new CorsFilter(src);
  }
}
