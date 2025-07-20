package br.com.ecommerce.meninadourada.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração CORS (Cross-Origin Resource Sharing) para a aplicação Spring Boot.
 * Permite que o frontend (rodando em uma origem diferente) acesse os endpoints da API.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Adiciona regras CORS para permitir requisições do frontend.
     *
     * @param registry O registro CORS para adicionar as configurações.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica as regras CORS a todos os endpoints que começam com /api/
                .allowedOrigins("http://localhost:3000", "http://localhost:5173", "http://18.228.9.73:9090") // Permite requisições destas origens (URLs do seu frontend local e do servidor)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite estes métodos HTTP
                .allowedHeaders("*") // Permite todos os cabeçalhos
                .allowCredentials(true) // Permite o envio de cookies de credenciais (se usados)
                .maxAge(3600); // Define por quanto tempo a resposta de pré-voo pode ser armazenada em cache
    }
}
