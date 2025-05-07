package com.wowapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilita la protección CSRF (Cross-Site Request Forgery)
            .csrf(csrf -> csrf.disable())
            // Configura las reglas de autorización para las solicitudes HTTP
            .authorizeHttpRequests(auth -> auth
                // Permite el acceso sin autenticación a las rutas especificadas
                .requestMatchers(
                    "/", "/index.html", "/login.html", "/registro.html",
                    "/css/**", "/js/**", "/api/**", "/oauth/**", "/api/perfil-menu"
                ).permitAll()
                // Requiere autenticación para cualquier otra solicitud
                .anyRequest().authenticated()
            )
            // Configura la página de inicio de sesión personalizada
            .formLogin(form -> form
                .loginPage("/login").permitAll()
            )
            // Permite el cierre de sesión sin restricciones
            .logout(logout -> logout.permitAll());

        // Construye y devuelve la cadena de filtros de seguridad
        return http.build();
    }
}
