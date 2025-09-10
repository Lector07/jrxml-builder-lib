package pl.lib.config;

import pl.lib.model.Calculation;

import java.util.Objects;

/**
 * Data grouping definition in a report.
 *
 * <p>This class defines how records are grouped in the report based on values
 * of a specified field. It allows configuration of group headers, footers, sorting
 * and aggregate calculations at group level.</p>
 *
 * <p>Uses Builder pattern for convenient creation of instances with optional fields.</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * GroupDefinition group = GroupDefinition.builder("category")
 *     .label("Product Category: ")
 *     .ascending(true)
 *     .showHeader(true)
 *     .showFooter(true)
 *     .showSummaryInHeader(false)
 *     .groupCalculation(Calculation.SUM)
 *     .build();
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see Calculation
 */
public class GroupDefinition {
    private String field;
    private String label;
    private boolean ascending;
    private boolean showHeader;
    private boolean showFooter;
    private boolean showSummaryInHeader;
    private Calculation groupCalculation;

    /**
     * Default constructor for JSON deserialization.
     */
    public GroupDefinition(){

    }

    /**
     * Private constructor used by Builder.
     *
     * @param builder builder instance with set values
     */
    private GroupDefinition(Builder builder) {
        this.field = Objects.requireNonNull(builder.field, "field");
        this.label = builder.label;
        this.ascending = builder.ascending;
        this.showHeader = builder.showHeader;
        this.showFooter = builder.showFooter;
        this.showSummaryInHeader = builder.showSummaryInHeader;
        this.groupCalculation = builder.groupCalculation;
    }

    /**
     * Returns the type of calculations performed at group level.
     *
     * @return group calculation type
     */
    public Calculation getGroupCalculation() {
        return groupCalculation;
    }

    /**
     * Returns the field name used for grouping.
     *
     * @return grouping field name
     */
    public String getField() {
        return field;
    }

    /**
     * Returns the label displayed in group header.
     *
     * @return group label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Checks if group sorting is ascending.
     *
     * @return true for ascending sort, false for descending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Checks if group header should be displayed.
     *
     * @return true if header should be displayed
     */
    public boolean isShowHeader() {
        return showHeader;
    }

    /**
     * Checks if group footer should be displayed.
     *
     * @return true if footer should be displayed
     */
    public boolean isShowFooter() {
        return showFooter;
    }

    /**
     * Checks if summaries should be shown in group header.
     *
     * @return true if summaries should be in header
     */
    public boolean isShowSummaryInHeader() {
        return showSummaryInHeader;
    }

    /**
     * Creates a new Builder for building GroupDefinition instance.
     *
     * @param field grouping field name (required)
     * @return new Builder
     */
    public static Builder builder(String field) {
        return new Builder(field);
    }

    /**
     * Builder for GroupDefinition class implementing Builder pattern.
     *
     * <p>Enables step-by-step building of GroupDefinition object with optional fields.</p>
     */
    public static class Builder {
        private final String field;
        private String label;
        private boolean ascending = true;
        private boolean showHeader = true;
        private boolean showFooter = false;
        private boolean showSummaryInHeader;
        private Calculation groupCalculation;

        /**
         * Builder constructor with required field name.
         *
         * @param field grouping field name (cannot be null)
         */
        public Builder(String field) {
            this.field = field;
        }

        /**
         * Sets the label displayed in group header.
         *
         * @param label group label
         * @return this Builder (for method chaining)
         */
        public Builder label(String label) {
            this.label = label;
            return this;
        }

        /**
         * Sets group sorting direction.
         *
         * @param ascending true for ascending sort, false for descending
         * @return this Builder (for method chaining)
         */
        public Builder ascending(boolean ascending) {
            this.ascending = ascending;
            return this;
        }

        /**
         * Sets whether group header should be displayed.
         *
         * @param showHeader true to display header
         * @return this Builder (for method chaining)
         */
        public Builder showHeader(boolean showHeader) {
            this.showHeader = showHeader;
            return this;
        }

        /**
         * Sets whether group footer should be displayed.
         *
         * @param showFooter true to display footer
         * @return this Builder (for method chaining)
         */
        public Builder showFooter(boolean showFooter) {
            this.showFooter = showFooter;
            return this;
        }

        /**
         * Sets whether summaries should be shown in group header.
         *
         * @param showSummaryInHeader true to show summaries in header
         * @return this Builder (for method chaining)
         */
        public Builder showSummaryInHeader(boolean showSummaryInHeader) {
            this.showSummaryInHeader = showSummaryInHeader;
            return this;
        }

        /**
         * Sets the type of calculations performed at group level.
         *
         * @param groupCalculation group calculation type
         * @return this Builder (for method chaining)
         */
        public Builder groupCalculation(Calculation groupCalculation){
            this.groupCalculation = groupCalculation;
            return this;
        }

        /**
         * Builds the final GroupDefinition instance.
         *
         * @return new GroupDefinition instance with set values
         */
        public GroupDefinition build() {
            return new GroupDefinition(this);
        }
    }
}
