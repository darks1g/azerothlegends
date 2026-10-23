package com.wowapp.controller;

import com.wowapp.model.Usuario;
import com.wowapp.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoginControllerTest {

    private static final String EMAIL = "jugador@example.com";
    private static final String SECRET = "test-shared-secret";

    private UsuarioRepository usuarioRepository;
    private LoginController controller;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        controller = new LoginController(usuarioRepository);
        ReflectionTestUtils.setField(controller, "verificationSecret", SECRET);

        usuario = new Usuario();
        usuario.setEmail(EMAIL);
        usuario.setTipo("web");
        usuario.setEsVerificado(false);
        usuario.setPasswordHash(new BCryptPasswordEncoder().encode("Contraseña1!"));
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void loginLeavesSessionUnauthenticatedUntilSecondFactorIsConfirmed() {
        HttpSession session = new MockHttpSession();
        LoginController.LoginRequest login = new LoginController.LoginRequest();
        login.email = EMAIL;
        login.password = "Contraseña1!";

        var response = controller.login(login, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(session.getAttribute("usuario"));
        assertEquals(EMAIL, session.getAttribute("login_pendiente_email"));
    }

    @Test
    void confirmationRejectsInvalidSignatureWithoutAuthenticating() throws Exception {
        HttpSession session = iniciarLogin();
        LoginController.VerificacionRequest proof = proof("jugador@example.com", "0".repeat(32));
        proof.signature = "0".repeat(64);

        var response = controller.confirmarVerificacion(proof, mock(HttpServletRequest.class), session);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(session.getAttribute("usuario"));
    }

    @Test
    void validProofAuthenticatesAndConsumesPendingLogin() throws Exception {
        HttpSession session = iniciarLogin();
        LoginController.VerificacionRequest proof = proof(EMAIL, "0123456789abcdef0123456789abcdef");
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.changeSessionId()).thenReturn("rotated-session-id");

        var response = controller.confirmarVerificacion(proof, servletRequest, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(usuario, session.getAttribute("usuario"));
        assertTrue(usuario.getEsVerificado());
        assertNull(session.getAttribute("login_pendiente_email"));
        verify(servletRequest).changeSessionId();
        verify(usuarioRepository).save(usuario);
    }

    private HttpSession iniciarLogin() {
        LoginController.LoginRequest login = new LoginController.LoginRequest();
        login.email = EMAIL;
        login.password = "Contraseña1!";

        MockHttpSession session = new MockHttpSession();
        controller.login(login, session);
        return session;
    }

    private static LoginController.VerificacionRequest proof(String email, String nonce) throws Exception {
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String message = email + "\n" + timestamp + "\n" + nonce;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

        LoginController.VerificacionRequest request = new LoginController.VerificacionRequest();
        request.email = email;
        request.timestamp = timestamp;
        request.nonce = nonce;
        request.signature = HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        return request;
    }
}
