package pl.lib.model;

public final class Style {
    private final String name;
    private final String fontName;
    private final float fontSize;
    private final boolean isBold;
    private final String fontColor;
    private final String backColor;
    private final String horizontalAlignment;
    private final String verticalAlignment;
    private final Integer padding;
    private final float borderWidth;
    private final String borderColor;

    public Style(String name) {
        this.name = name;
        this.fontName = "DejaVu Sans";
        this.fontSize = 8;
        this.isBold = false;
        this.fontColor = "#000000";
        this.backColor = null;
        this.horizontalAlignment = "Left";
        this.verticalAlignment = "Middle";
        this.padding = null;
        this.borderWidth = 0f;
        this.borderColor = "#000000";
    }

    private Style(String name, String fontName, float fontSize, boolean isBold, String fontColor, String backColor,
                  String horizontalAlignment, String verticalAlignment, Integer padding, float borderWidth, String borderColor) {
        this.name = name;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.isBold = isBold;
        this.fontColor = fontColor;
        this.backColor = backColor;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.padding = padding;
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
    }

    public Style withFont(String fontName, int size, boolean isBold) {
        return new Style(this.name, fontName, size, isBold, this.fontColor, this.backColor, this.horizontalAlignment,
                this.verticalAlignment, this.padding, this.borderWidth, this.borderColor);
    }

    public Style withColors(String fontColor, String backColor) {
        return new Style(this.name, this.fontName, this.fontSize, this.isBold, fontColor != null ? fontColor : this.fontColor,
                backColor, this.horizontalAlignment, this.verticalAlignment, this.padding, this.borderWidth, this.borderColor);
    }

    public Style withAlignment(String horizontalAlignment, String verticalAlignment) {
        return new Style(this.name, this.fontName, this.fontSize, this.isBold, this.fontColor, this.backColor,
                horizontalAlignment, verticalAlignment, this.padding, this.borderWidth, this.borderColor);
    }

    public Style withBorders(float width, String color) {
        return new Style(this.name, this.fontName, this.fontSize, this.isBold, this.fontColor, this.backColor,
                this.horizontalAlignment, this.verticalAlignment, this.padding, width, color != null ? color : this.borderColor);
    }

    public Style withPadding(int padding) {
        return new Style(this.name, this.fontName, this.fontSize, this.isBold, this.fontColor, this.backColor,
                this.horizontalAlignment, this.verticalAlignment, padding, this.borderWidth, this.borderColor);
    }

    // Gettery
    public String getName() { return name; }
    public String getFontName() { return fontName; }
    public float getFontSize() { return fontSize; }
    public boolean isBold() { return isBold; }
    public String getFontColor() { return fontColor; }
    public String getBackColor() { return backColor; }
    public String getHorizontalAlignment() { return horizontalAlignment; }
    public String getVerticalAlignment() { return verticalAlignment; }
    public float getBorderWidth() { return borderWidth; }
    public String getBorderColor() { return borderColor; }
    public Integer getPadding() { return padding; }
}