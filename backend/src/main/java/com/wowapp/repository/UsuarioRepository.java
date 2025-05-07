package com.wowapp.repository;

import com.wowapp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Repositorio para la entidad Usuario, que extiende JpaRepository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Método para buscar un usuario por su correo electrónico
    Optional<Usuario> findByEmail(String email);

    // Método para buscar un usuario por su battletag
    Optional<Usuario> findByBattletag(String battletag);

}
