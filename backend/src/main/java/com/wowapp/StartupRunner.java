package com.wowapp;

import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.repository.ReinoRepository;
import com.wowapp.service.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final ApiService apiService;
    private final ReinoRepository reinoRepository;

    public StartupRunner(ApiService apiService, ReinoRepository reinoRepository) {
        this.apiService = apiService;
        this.reinoRepository = reinoRepository;
    }

    @Override
    public void run(String... args) {
        // Los reinos casi nunca cambian: solo se descargan si todavía no están en la base de datos
        for (VersionJuego version : VersionJuego.values()) {
            if (reinoRepository.existsByRegionAndVersionJuego("eu", version)) {
                log.info("Reinos de eu / {} ya en la base de datos.", version);
            } else {
                log.info("Descargando reinos de eu / {}...", version);
                apiService.poblarReinosDesdeAPI("eu", version);
            }
        }
    }
}
