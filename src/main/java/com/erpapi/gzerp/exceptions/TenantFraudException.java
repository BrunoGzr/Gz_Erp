package com.erpapi.gzerp.exceptions;

public class TenantFraudException extends RuntimeException {
    public TenantFraudException(String message) {
        super(message);
    }
}
