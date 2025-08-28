package pl.lib.model;

public class Group {
    private final String fieldName;
    private final String headerExpression;
    private String styleName;
    private boolean showGroupFooter;


    public Group(String fieldName, String headerExpression, String styleName, boolean showGroupFooter) {
        this.fieldName = fieldName;
        this.headerExpression = headerExpression;
        this.styleName = styleName;
        this.showGroupFooter = showGroupFooter;
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

    public Group withShowGroupFooter(boolean show) {
        this.showGroupFooter = show;
        return this;
    }

    public boolean isShowGroupFooter() {
        return showGroupFooter;
    }

    public boolean isShowGroupHeader() {
        return true;
    }
}