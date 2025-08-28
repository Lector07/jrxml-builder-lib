package pl.lib.config;

import java.util.Objects;


public class GroupDefinition {
    private final String field;
    private final String label;
    private final boolean ascending;
    private final boolean showHeader;
    private final boolean showFooter;

    private GroupDefinition(Builder builder) {
        this.field = Objects.requireNonNull(builder.field, "field");
        this.label = builder.label;
        this.ascending = builder.ascending;
        this.showHeader = builder.showHeader;
        this.showFooter = builder.showFooter;
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

    public static Builder builder(String field) {
        return new Builder(field);
    }

    public static class Builder {
        private final String field;
        private String label;
        private boolean ascending = true;
        private boolean showHeader = true;
        private boolean showFooter = false;

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

        public GroupDefinition build() {
            return new GroupDefinition(this);
        }
    }
}
