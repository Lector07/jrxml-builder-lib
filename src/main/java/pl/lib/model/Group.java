package pl.lib.model;

public class Group {
    private final String fieldName;
    private final String headerExpression;
    private String styleName;

    public Group(String fieldName, String headerExpression) {
        this.fieldName = fieldName;
        this.headerExpression = headerExpression;
    }

    public Group withHeaderStyle(String styleName) {
        this.styleName = styleName;
        return this;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getHeaderExpression() {
        return headerExpression;
    }

    public String getStyleName() {
        return styleName;
    }

    public boolean hasStyle() {
        return this.styleName != null && !this.styleName.isEmpty();
    }
}