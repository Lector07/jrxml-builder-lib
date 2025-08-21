package pl.lib.model;

public class Group {
    private final String fieldName;

    public Group(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty.");
        }
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
