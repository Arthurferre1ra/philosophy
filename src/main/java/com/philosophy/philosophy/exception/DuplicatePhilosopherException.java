package com.philosophy.philosophy.exception;

public class DuplicatePhilosopherException extends RuntimeException {
    public DuplicatePhilosopherException() {
        super("Já existe um filósofo com esse nome neste período. Edite o registro existente ou informe outro nome.");
    }
}
