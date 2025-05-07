package com.wowapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Indica que esta clase es una configuración de Spring
public class CorsConfig {
    @Bean // Define un bean que será gestionado por el contenedor de Spring
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Configura las reglas de CORS (Cross-Origin Resource Sharing)
                registry.addMapping("/api/**") // Aplica las reglas a las rutas que comienzan con /api/
                        .allowedOrigins("http://localhost:5500", "http://azerothlegends.sytes.net") // Permite solicitudes desde estos orígenes
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // Métodos HTTP permitidos
                        .allowedHeaders("*"); // Permite todos los encabezados
            }
        };
    }
}
