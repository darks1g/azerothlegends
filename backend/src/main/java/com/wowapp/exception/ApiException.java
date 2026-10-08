package com.wowapp.exception;

import org.springframework.http.HttpStatus;

// Error "esperado" con un mensaje pensado para mostrarse al usuario
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
