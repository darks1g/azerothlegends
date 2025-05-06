package com.wowapp.service;

import com.wowapp.model.Usuario;
import com.wowapp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado.");
        }

        String hash = BCrypt.hashpw(usuario.getPasswordHash(), BCrypt.gensalt());
        usuario.setPasswordHash(hash);
        usuario.setTipo("web");
        usuario.setEsVerificado(false);

        return usuarioRepository.save(usuario);
    }

    public boolean verificarCredenciales(String email, String passwordPlano) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            return BCrypt.checkpw(passwordPlano, usuario.getPasswordHash());
        }
        return false;
    }

    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}
