package pl.lib.config;

/**
 * Cell highlighting rule in a report.
 *
 * <p>This class defines conditions and formatting for automatic cell highlighting
 * in reports based on data field values.</p>
 *
 * <h3>Available operators:</h3>
 * <ul>
 *   <li><code>EQUALS</code> - equal values</li>
 *   <li><code>NOT_EQUALS</code> - different values</li>
 *   <li><code>CONTAINS</code> - contains text (only for text fields)</li>
 *   <li><code>GREATER_THAN</code> - greater than (only for numeric fields)</li>
 *   <li><code>LESS_THAN</code> - less than (only for numeric fields)</li>
 * </ul>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * // Highlight red for values greater than 1000
 * HighlightRule rule = new HighlightRule();
 * rule.setField("price");
 * rule.setOperator("GREATER_THAN");
 * rule.setValue("1000");
 * rule.setColor("#FFCCCC");
 *
 * // Highlight blue for products containing "Premium"
 * HighlightRule rule2 = new HighlightRule();
 * rule2.setField("name");
 * rule2.setOperator("CONTAINS");
 * rule2.setValue("Premium");
 * rule2.setColor("#CCE5FF");
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see FormattingOptions
 */
public class HighlightRule {
    private String id;
    private String field;
    private String operator;
    private String value;
    private String color;

    /**
     * Default constructor for JSON deserialization.
     */
    public HighlightRule(){

    }

    /**
     * Returns the unique identifier of the rule.
     *
     * @return rule ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the rule.
     *
     * @param id unique identifier for the rule
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the field name to check.
     *
     * @return data field name
     */
    public String getField() {
        return field;
    }

    /**
     * Sets the field name to check.
     *
     * @param field field name in source data
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * Returns the comparison operator.
     *
     * @return operator (EQUALS, NOT_EQUALS, CONTAINS, GREATER_THAN, LESS_THAN)
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Sets the comparison operator.
     *
     * @param operator comparison operator (EQUALS, NOT_EQUALS, CONTAINS, GREATER_THAN, LESS_THAN)
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * Returns the value to compare against.
     *
     * @return reference value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value to compare against.
     *
     * @param value value to compare with the field
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the highlight color in hex format.
     *
     * @return color in #RRGGBB format
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the highlight color.
     *
     * @param color color in hex format (#RRGGBB) for cells meeting the condition
     */
    public void setColor(String color) {
        this.color = color;
    }
}
