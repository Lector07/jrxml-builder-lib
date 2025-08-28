package pl.lib.config;

import java.util.Objects;

public class ColumnDefinition {
    private final String field;
    private final String header;
    private final Integer width;
    private final String format;

    private ColumnDefinition(Builder builder) {
        this.field = Objects.requireNonNull(builder.field, "field");
        this.header = builder.header != null ? builder.header : this.field;
        this.width = builder.width;
        this.format = builder.format;
    }

    public String getField() {
        return field;
    }

    public String getHeader() {
        return header;
    }

    public Integer getWidth() {
        return width;
    }

    public String getFormat() {
        return format;
    }

    public static Builder builder(String field) {
        return new Builder(field);
    }

    public static class Builder {
        private final String field;
        private String header;
        private Integer width;
        private String format;

        public Builder(String field) {
            this.field = field;
        }

        public Builder header(String header) {
            this.header = header;
            return this;
        }

        public Builder width(Integer width) {
            this.width = width;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public ColumnDefinition build() {
            return new ColumnDefinition(this);
        }
    }
}
