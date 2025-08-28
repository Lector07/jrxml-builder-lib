package pl.lib.config;

import pl.lib.model.Calculation;

import java.util.Objects;


public class GroupDefinition {
    private final String field;
    private final String label;
    private final boolean ascending;
    private final boolean showHeader;
    private final boolean showFooter;
    private final boolean showSummaryInHeader;
    private final Calculation groupCalculation;



    private GroupDefinition(Builder builder) {
        this.field = Objects.requireNonNull(builder.field, "field");
        this.label = builder.label;
        this.ascending = builder.ascending;
        this.showHeader = builder.showHeader;
        this.showFooter = builder.showFooter;
        this.showSummaryInHeader = builder.showSummaryInHeader;
        this.groupCalculation = builder.groupCalculation;
    }

    public Calculation getGroupCalculation() {
        return groupCalculation;
    }

    public String getField() {
        return field;
    }

    public String getLabel() {
        return label;
    }

    public boolean isAscending() {
        return ascending;
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public boolean isShowFooter() {
        return showFooter;
    }

    public boolean isShowSummaryInHeader() {
        return showSummaryInHeader;
    }




    public static Builder builder(String field) {
        return new Builder(field);
    }

    public static class Builder {
        private final String field;
        private String label;
        private boolean ascending = true;
        private boolean showHeader = true;
        private boolean showFooter = false;
        private boolean showSummaryInHeader;
        private Calculation groupCalculation;


        public Builder(String field) {
            this.field = field;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder ascending(boolean ascending) {
            this.ascending = ascending;
            return this;
        }

        public Builder showHeader(boolean showHeader) {
            this.showHeader = showHeader;
            return this;
        }

        public Builder showFooter(boolean showFooter) {
            this.showFooter = showFooter;
            return this;
        }

        public Builder showSummaryInHeader(boolean showSummaryInHeader) {
            this.showSummaryInHeader = showSummaryInHeader;
            return this;
        }

        public Builder groupCalculation(Calculation groupCalculation){
            this.groupCalculation = groupCalculation;
            return this;
        }

        public GroupDefinition build() {
            return new GroupDefinition(this);
        }
    }
}
