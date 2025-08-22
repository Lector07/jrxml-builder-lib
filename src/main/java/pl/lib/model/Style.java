package pl.lib.model;

public class Style {
    private final String name;
    private String fontName = "DejaVu Sans";
    private int fontSize = 8;
    private boolean isBold = false;
    private String fontColor = "#000000";
    private String backColor; // null = przezroczysty
    private String horizontalAlignment = "Left";
    private String verticalAlignment = "Middle";

    public Style(String name) {
        this.name = name;
    }

    public Style withFontName(String fontName, int size, boolean isBold) {
        this.fontName = fontName;
        this.fontSize = size;
        this.isBold = isBold;
        return this;
    }

    public Style withColors(String fontColor, String backColor) {
        this.fontColor = fontColor;
        this.backColor = backColor;
        return this;
    }

    public Style withAlignment(String horizontalAlignment, String verticalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getFontName() {
        return fontName;
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean isBold() {
        return isBold;
    }

    public String getFontColor() {
        return fontColor;
    }

    public String getBackColor() {
        return backColor;
    }

    public String getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public String getVerticalAlignment() {
        return verticalAlignment;
    }

}