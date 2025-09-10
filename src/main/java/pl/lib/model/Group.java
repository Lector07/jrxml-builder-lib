package pl.lib.model;

/**
 * Represents a group definition in a report.
 *
 * <p>This class defines how data should be grouped in the report, including
 * the grouping field, header expression, styling, and display options for
 * group headers and footers.</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * Group categoryGroup = new Group(
 *     "category",                // field to group by
 *     "Category: " + $F{category}, // header expression
 *     "groupHeaderStyle",        // style name
 *     true,                      // show group footer
 *     true                       // show group header
 * );
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see pl.lib.config.GroupDefinition
 */
public final class Group {
    private final String fieldName;
    private final String headerExpression;
    private final String styleName;
    private final boolean showGroupFooter;
    private final boolean showHeader;

    /**
     * Creates a new Group with specified properties.
     *
     * @param fieldName name of the field to group by
     * @param headerExpression expression for the group header
     * @param styleName name of the style to apply to group header
     * @param showGroupFooter whether to show group footer
     * @param showHeader whether to show group header
     */
    public Group(String fieldName, String headerExpression, String styleName, boolean showGroupFooter, boolean showHeader) {
        this.fieldName = fieldName;
        this.headerExpression = headerExpression;
        this.styleName = styleName;
        this.showGroupFooter = showGroupFooter;
        this.showHeader = showHeader;
    }

    /**
     * Creates a copy of this group with modified header style.
     *
     * @param styleName new style name for the header
     * @return new Group instance with updated style
     */
    public Group withHeaderStyle(String styleName) {
        return new Group(this.fieldName, this.headerExpression, styleName, this.showGroupFooter, this.showHeader);
    }

    /**
     * Creates a copy of this group with modified footer visibility.
     *
     * @param show whether to show group footer
     * @return new Group instance with updated footer setting
     */
    public Group withShowGroupFooter(boolean show) {
        return new Group(this.fieldName, this.headerExpression, this.styleName, show, this.showHeader);
    }

    /**
     * Returns the field name used for grouping.
     *
     * @return grouping field name
     */
    public String getFieldName() { return fieldName; }

    /**
     * Returns the header expression for the group.
     *
     * @return header expression
     */
    public String getHeaderExpression() { return headerExpression; }

    /**
     * Returns the style name for the group header.
     *
     * @return style name
     */
    public String getStyleName() { return styleName; }

    /**
     * Checks if this group has a style defined.
     *
     * @return true if style name is defined and not empty
     */
    public boolean hasStyle() { return this.styleName != null && !this.styleName.isEmpty(); }

    /**
     * Checks if group footer should be displayed.
     *
     * @return true if group footer should be shown
     */
    public boolean isShowGroupFooter() { return showGroupFooter; }

    /**
     * Checks if group header should be displayed.
     *
     * @return true if group header should be shown
     */
    public boolean isShowGroupHeader() { return showHeader; }
}