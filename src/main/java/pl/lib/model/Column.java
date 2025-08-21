package pl.lib.model;

public class Column {
    private final String fieldName;
    private final String title;
    private final int width;

    public Column(String fieldName, String title, int width) {
        this.fieldName = fieldName;
        this.title = title;
        this.width = width;
    }

    public String getFieldName() {
        return fieldName;
    }

    /**
     * @deprecated Literówka w poprzedniej wersji. Użyj {@link #getFieldName()}.
     */
    @Deprecated
    public String getFielsdName() {
        return fieldName;
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }
}