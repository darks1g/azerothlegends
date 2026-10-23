package com.wowapp.repository;

import com.wowapp.model.PersonajeListado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PersonajeListadoRepository extends JpaRepository<PersonajeListado, Long>,
        JpaSpecificationExecutor<PersonajeListado> {

    Optional<PersonajeListado> findByOwnerKeyAndPersonajeId(String ownerKey, Long personajeId);

    List<PersonajeListado> findAllByOwnerKey(String ownerKey);
}
