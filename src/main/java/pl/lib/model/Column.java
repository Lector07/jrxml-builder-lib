package pl.lib.model;

public class Column {
    private final String fieldName;
    private final String title;
    private final int width;
    private final DataType type;
    private final String pattern;

    public Column(String fieldName, String title, int width, DataType type, String pattern) {
        this.fieldName = fieldName;
        this.title = title;
        this.width = width;
        this.type = type;
        this.pattern = pattern;

    }

    public String getFieldName() {
        return fieldName;
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public DataType getType() {
        return type;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean hasPattern() { return pattern != null && !pattern.isEmpty(); }

}