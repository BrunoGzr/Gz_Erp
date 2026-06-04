package com.erpapi.gzerp.Exceptions;

public class EmptyResultDataAccessExceptionCustom extends RuntimeException {


    public EmptyResultDataAccessExceptionCustom() {
    }

    public EmptyResultDataAccessExceptionCustom(String message) {
        super(message);
    }
    public EmptyResultDataAccessExceptionCustom(String message, Throwable cause) {
        super(message, cause);
    }
}
