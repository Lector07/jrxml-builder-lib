package pl.lib.model;

public class Group {
    private final String fieldName;
    private final String headerExpression;

    public Group(String fieldName, String headerExpression) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty.");
        }
        this.fieldName = fieldName;
        this.headerExpression = (headerExpression != null && !headerExpression.isEmpty()) ? headerExpression : "$F{" + fieldName + "}";
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getHeaderExpression() {
        return headerExpression;
    }
}
