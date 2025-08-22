package pl.lib.model;

public class Column {
    private final String fieldName;
    private final String title;
    private final int width;
    private final DataType type;
    private final String pattern;
    private final Boolean summed;
    private final Boolean summedInGroup;

    public Column(String fieldName, String title, int width, DataType type, String pattern, Boolean summed, Boolean summedInGroup) {
        this.fieldName = fieldName;
        this.title = title;
        this.width = width;
        this.type = type;
        this.pattern = pattern;
        this.summed = summed;
        this.summedInGroup = summedInGroup;

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

    public boolean hasPattern() {
        return pattern != null && !pattern.isEmpty();
    }

    public Boolean isSummed() {
        return summed;
    }

    public Boolean isSummedInGroup() {
        return summedInGroup;
    }

}