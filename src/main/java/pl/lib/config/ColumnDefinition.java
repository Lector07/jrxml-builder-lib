package pl.lib.config;

import pl.lib.model.Calculation;

import java.util.Objects;

public class ColumnDefinition {
    private String field;
    private String header;
    private Integer width = -1;
    private String format;
    private Calculation reportCalculation;
    private Calculation groupCalculation;
    private Boolean visible;

    public ColumnDefinition(){

    }

    private ColumnDefinition(Builder builder) {
        this.field = Objects.requireNonNull(builder.field, "field");
        this.header = builder.header != null ? builder.header : this.field;
        this.width = builder.width;
        this.format = builder.format;
        this.reportCalculation = builder.reportCalculation;
        this.groupCalculation = builder.groupCalculation;
        this.visible = builder.visible;
    }

    public boolean isVisible() {
        return visible;
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

    public Calculation getReportCalculation() {
        return reportCalculation;
    }

    public Calculation getGroupCalculation() {
        return groupCalculation;
    }

    public Boolean getVisible() {
        return visible;
    }

    public static Builder builder(String field) {
        return new Builder(field);
    }

    public static class Builder {
        private final String field;
        private String header;
        private Integer width;
        private String format;
        private Calculation reportCalculation;
        private Calculation groupCalculation;
        private Boolean visible;


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

        public Builder reportCalculation(Calculation reportCalculation){
            this.reportCalculation = reportCalculation;
            return this;
        }

        public Builder groupCalculation(Calculation groupCalculation){
            this.groupCalculation = groupCalculation;
            return this;
        }

        public Builder visible(Boolean visible){
            this.visible = visible;
            return this;
        }

        public ColumnDefinition build() {
            return new ColumnDefinition(this);
        }
    }
}
