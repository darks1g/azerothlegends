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

    public StartupRunner(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void run(String... args) {
        System.out.println("Poblando tabla de reinos al arrancar...");
        apiService.poblarReinosDesdeAPI("eu", VersionJuego.retail);
        apiService.poblarReinosDesdeAPI("eu", VersionJuego.classic_era);
        apiService.poblarReinosDesdeAPI("eu", VersionJuego.classic);
    }
}
