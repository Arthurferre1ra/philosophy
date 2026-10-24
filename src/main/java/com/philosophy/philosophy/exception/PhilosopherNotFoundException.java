package com.philosophy.philosophy.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PhilosopherNotFoundException extends RuntimeException {
    public PhilosopherNotFoundException(UUID id) {
        super("Filósofo não encontrado: " + id);
    }
}
