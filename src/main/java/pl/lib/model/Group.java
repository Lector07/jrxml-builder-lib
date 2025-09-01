package pl.lib.model;

public final class Group {
    private final String fieldName;
    private final String headerExpression;
    private final String styleName;
    private final boolean showGroupFooter;
    private final boolean showHeader;

    public Group(String fieldName, String headerExpression, String styleName, boolean showGroupFooter, boolean showHeader) {
        this.fieldName = fieldName;
        this.headerExpression = headerExpression;
        this.styleName = styleName;
        this.showGroupFooter = showGroupFooter;
        this.showHeader = showHeader;
    }

    public Group withHeaderStyle(String styleName) {
        return new Group(this.fieldName, this.headerExpression, styleName, this.showGroupFooter, this.showHeader);
    }

    public Group withShowGroupFooter(boolean show) {
        return new Group(this.fieldName, this.headerExpression, this.styleName, show, this.showHeader);
    }

    // Gettery
    public String getFieldName() { return fieldName; }
    public String getHeaderExpression() { return headerExpression; }
    public String getStyleName() { return styleName; }
    public boolean hasStyle() { return this.styleName != null && !this.styleName.isEmpty(); }
    public boolean isShowGroupFooter() { return showGroupFooter; }
    public boolean isShowGroupHeader() { return showHeader; }
}