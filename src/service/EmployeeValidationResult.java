package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EmployeeValidationResult {

    private final List<FieldError> errors;

    public EmployeeValidationResult(List<FieldError> errors) {
        this.errors = errors == null ? Collections.emptyList() : List.copyOf(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public List<String> getMessages() {
        List<String> messages = new ArrayList<>();
        for (FieldError error : errors) {
            messages.add(error.getMessage());
        }
        return messages;
    }

    public static final class FieldError {
        private final String field;
        private final String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}