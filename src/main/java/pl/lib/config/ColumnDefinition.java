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
    private String type = "COLUMN"; 
    public ColumnDefinition() {
    }
    private ColumnDefinition(Builder builder) {
        this.field = Objects.requireNonNull(builder.field, "field");
        this.header = builder.header != null ? builder.header : this.field;
        this.width = builder.width;
        this.format = builder.format;
        this.reportCalculation = builder.reportCalculation;
        this.groupCalculation = builder.groupCalculation;
        this.visible = builder.visible;
        this.type = builder.type != null ? builder.type : this.type;
    }
    public static Builder builder(String field) {
        return new Builder(field);
    }
    public boolean isVisible() {
        return visible;
    }
    public String getField() {
        return field;
    }
    public void setField(String field) {
        this.field = field;
    }
    public String getHeader() {
        return header;
    }
    public void setHeader(String header) {
        this.header = header;
    }
    public Integer getWidth() {
        return width;
    }
    public void setWidth(Integer width) {
        this.width = width;
    }
    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }
    public Calculation getReportCalculation() {
        return reportCalculation;
    }
    public void setReportCalculation(Calculation reportCalculation) {
        this.reportCalculation = reportCalculation;
    }
    public Calculation getGroupCalculation() {
        return groupCalculation;
    }
    public void setGroupCalculation(Calculation groupCalculation) {
        this.groupCalculation = groupCalculation;
    }
    public Boolean getVisible() {
        return visible;
    }
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public static class ColumnType {
        public static final String COLUMN = "COLUMN";
        public static final String SUBREPORT = "SUBREPORT";
    }
    public static class Builder {
        private final String field;
        private String header;
        private Integer width;
        private String format;
        private Calculation reportCalculation;
        private Calculation groupCalculation;
        private Boolean visible;
        private String type;
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
        public Builder reportCalculation(Calculation reportCalculation) {
            this.reportCalculation = reportCalculation;
            return this;
        }
        public Builder groupCalculation(Calculation groupCalculation) {
            this.groupCalculation = groupCalculation;
            return this;
        }
        public Builder visible(Boolean visible) {
            this.visible = visible;
            return this;
        }
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        public ColumnDefinition build() {
            return new ColumnDefinition(this);
        }
    }
}
