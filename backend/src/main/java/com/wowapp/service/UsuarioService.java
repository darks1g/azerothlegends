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

    // Registra un nuevo usuario en el sistema
    public Usuario registrar(Usuario usuario) {
        // Verifica si el correo ya está registrado
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado.");
        }

        // Genera un hash para la contraseña y configura los valores iniciales del usuario
        String hash = BCrypt.hashpw(usuario.getPasswordHash(), BCrypt.gensalt());
        usuario.setPasswordHash(hash);
        usuario.setTipo("web");
        usuario.setEsVerificado(false);

        // Guarda el usuario en el repositorio
        return usuarioRepository.save(usuario);
    }

    // Verifica las credenciales de un usuario
    public boolean verificarCredenciales(String email, String passwordPlano) {
        // Busca el usuario por correo electrónico
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Compara la contraseña proporcionada con el hash almacenado
            return BCrypt.checkpw(passwordPlano, usuario.getPasswordHash());
        }
        return false;
    }

    // Obtiene un usuario por su correo electrónico
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}
