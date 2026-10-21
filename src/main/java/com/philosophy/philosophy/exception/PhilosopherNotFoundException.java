package com.philosophy.philosophy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PhilosopherNotFoundException extends RuntimeException {
    public PhilosopherNotFoundException(long id) {
        super("Filósofo não encontrado: " + id);
    }
}
