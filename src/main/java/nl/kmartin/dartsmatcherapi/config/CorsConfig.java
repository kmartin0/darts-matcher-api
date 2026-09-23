package nl.kmartin.dartsmatcherapi.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configures cross-origin requests allowed by the application.
 */
@Configuration
public class CorsConfig {
    private static final List<String> ALLOWED_ORIGIN_PATTERNS = List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://192.168.1.*:*",
            "https://jiangxy.github.io", // WebSocket debug tool: /websocket-debug-tool/
            "https://dartsmatcher.kmartin.nl"
    );

    /**
     * Creates the CORS configuration used for all application endpoints.
     *
     * @return the configured CORS source
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS);
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Registers the CORS filter with the highest precedence.
     *
     * @param source the CORS configuration source
     * @return the CORS filter registration
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean(
            UrlBasedCorsConfigurationSource source
    ) {
        FilterRegistrationBean<CorsFilter> registration =
                new FilterRegistrationBean<>(new CorsFilter(source));

        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }
}