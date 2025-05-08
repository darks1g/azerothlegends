package com.wowapp.repository;

import com.wowapp.model.EquipoPersonaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
// Repositorio para gestionar las operaciones de la entidad EquipoPersonaje
public interface EquipoPersonajeRepository extends JpaRepository<EquipoPersonaje, Long> {
    
    // Método para eliminar registros por el ID del personaje
    void deleteByPersonajeId(Long personajeId);

    List<EquipoPersonaje> findByPersonajeId(Long personajeId);

}
