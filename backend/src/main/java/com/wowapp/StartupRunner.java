package com.wowapp;

import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.service.ApiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.model.Reino;
import com.wowapp.repository.ReinoRepository;

@Component
public class StartupRunner implements CommandLineRunner {

    private final ApiService apiService;

    // Constructor que inyecta el servicio ApiService
    public StartupRunner(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void run(String... args) {
        // Método que se ejecuta al iniciar la aplicación
        System.out.println("Poblando tabla de reinos al arrancar...");
        // Poblar la tabla de reinos con datos de la API para diferentes versiones del juego
        apiService.poblarReinosDesdeAPI("eu", VersionJuego.retail);
        apiService.poblarReinosDesdeAPI("eu", VersionJuego.classic_era);
        apiService.poblarReinosDesdeAPI("eu", VersionJuego.classic);
    }
}
