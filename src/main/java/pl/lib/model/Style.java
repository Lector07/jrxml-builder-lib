package pl.lib.model;

public class Style {
    private final String name;
    private String fontName = "DejaVu Sans";
    private float fontSize = 8;
    private boolean isBold = false;
    private String fontColor = "#000000";
    private String backColor;
    private String horizontalAlignment = "Left";
    private String verticalAlignment = "Middle";
    private boolean hasBox = false;

    private float borderWidth = 0f;
    private String borderColor = "#000000";

    public Style(String name) {
        this.name = name;
    }

    public Style withFont(String fontName, int size, boolean isBold) {
        this.fontName = fontName;
        this.fontSize = size;
        this.isBold = isBold;
        return this;
    }

    public Style withColors(String fontColor, String backColor) {
        if (fontColor != null) this.fontColor = fontColor;
        this.backColor = backColor;
        return this;
    }

    public Style withAlignment(String horizontalAlignment, String verticalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        return this;
    }

    public Style withBox(boolean enabled) {
        this.hasBox = enabled;
        return this;
    }

    public Style withBorders(float width, String color) {
        this.borderWidth = width;
        if (color != null) this.borderColor = color;
        return this;
    }

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
}