package com.erpapi.gzerp.Exceptions;

import java.util.ArrayList;
import java.util.List;

public class UserAlreadyExistException extends RuntimeException {
    private List<String> conflictedFields = new ArrayList<>();

    public UserAlreadyExistException(List<String> fields) {
        super("Conflicting fields: " + String.join(", ",fields));
        this.conflictedFields = fields;
    }

    public List<String> getConflictedFields() {
        return conflictedFields;
    }
}
