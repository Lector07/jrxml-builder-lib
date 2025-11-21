package pl.lib.config;

import pl.lib.model.ChartType;

/**
 * Konfiguracja wykresu w raporcie
 */
public class ChartConfig {
    private ChartType type;
    private String title;
    private String categoryField;
    private String valueField;
    private int width = 500;
    private int height = 300;
    private boolean showLegend = true;
    private boolean show3D = false;
    private String categoryAxisLabel;
    private String valueAxisLabel;

    public ChartConfig() {
    }

    public ChartConfig(ChartType type, String title, String categoryField, String valueField) {
        this.type = type;
        this.title = title;
        this.categoryField = categoryField;
        this.valueField = valueField;
    }

    public void setChartType(String type) {
    }

    // Builder pattern
    public static class Builder {
        private final ChartConfig config = new ChartConfig();

        public Builder type(ChartType type) {
            config.type = type;
            return this;
        }

        public Builder title(String title) {
            config.title = title;
            return this;
        }

        public Builder categoryField(String categoryField) {
            config.categoryField = categoryField;
            return this;
        }

        public Builder valueField(String valueField) {
            config.valueField = valueField;
            return this;
        }

        public Builder width(int width) {
            config.width = width;
            return this;
        }

        public Builder height(int height) {
            config.height = height;
            return this;
        }

        public Builder showLegend(boolean showLegend) {
            config.showLegend = showLegend;
            return this;
        }

        public Builder show3D(boolean show3D) {
            config.show3D = show3D;
            return this;
        }

        public Builder categoryAxisLabel(String label) {
            config.categoryAxisLabel = label;
            return this;
        }

        public Builder valueAxisLabel(String label) {
            config.valueAxisLabel = label;
            return this;
        }

        public ChartConfig build() {
            return config;
        }
    }

    // Getters and Setters
    public ChartType getType() {
        return type;
    }

    public void setType(ChartType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryField() {
        return categoryField;
    }

    public void setCategoryField(String categoryField) {
        this.categoryField = categoryField;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isShowLegend() {
        return showLegend;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    public boolean isShow3D() {
        return show3D;
    }

    public void setShow3D(boolean show3D) {
        this.show3D = show3D;
    }

    public String getCategoryAxisLabel() {
        return categoryAxisLabel;
    }

    public void setCategoryAxisLabel(String categoryAxisLabel) {
        this.categoryAxisLabel = categoryAxisLabel;
    }

    public String getValueAxisLabel() {
        return valueAxisLabel;
    }

    public void setValueAxisLabel(String valueAxisLabel) {
        this.valueAxisLabel = valueAxisLabel;
    }
}

