package pl.lib.model;

/**
 * Represents a visual style definition for report elements.
 *
 * <p>This immutable class defines styling properties for report elements including
 * fonts, colors, alignment, borders, and padding. Uses fluent interface pattern
 * for easy style configuration.</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * Style headerStyle = new Style("HeaderStyle")
 *     .withFont("Arial", 12, true)
 *     .withColors("#000000", "#E0E0E0")
 *     .withAlignment("Center", "Middle")
 *     .withBorders(1.0f, "#CCCCCC")
 *     .withPadding(5);
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see pl.lib.model.ReportStyles
 */
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

    /**
     * Creates a new Style with default properties.
     *
     * @param name unique name for the style
     */
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

    /**
     * Private constructor for creating style variations.
     *
     * @param name style name
     * @param fontName font family name
     * @param fontSize font size in points
     * @param isBold whether font is bold
     * @param fontColor font color in hex format
     * @param backColor background color in hex format
     * @param horizontalAlignment horizontal text alignment
     * @param verticalAlignment vertical text alignment
     * @param padding padding in pixels
     * @param borderWidth border width in points
     * @param borderColor border color in hex format
     */
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

    /**
     * Creates a new Style with modified font properties.
     *
     * @param fontName font family name (e.g., "Arial", "DejaVu Sans")
     * @param size font size in points
     * @param isBold whether the font should be bold
     * @return new Style instance with updated font properties
     */
    public Style withFont(String fontName, int size, boolean isBold) {
        return new Style(this.name, fontName, size, isBold, this.fontColor, this.backColor, this.horizontalAlignment,
                this.verticalAlignment, this.padding, this.borderWidth, this.borderColor);
    }

    /**
     * Creates a new Style with modified color properties.
     *
     * @param fontColor font color in hex format (e.g., "#000000") or null to keep current
     * @param backColor background color in hex format (e.g., "#FFFFFF") or null for transparent
     * @return new Style instance with updated color properties
     */
    public Style withColors(String fontColor, String backColor) {
        return new Style(this.name, this.fontName, this.fontSize, this.isBold, fontColor != null ? fontColor : this.fontColor,
                backColor, this.horizontalAlignment, this.verticalAlignment, this.padding, this.borderWidth, this.borderColor);
    }

    /**
     * Creates a new Style with modified text alignment properties.
     *
     * @param horizontalAlignment horizontal alignment ("Left", "Center", "Right", "Justified")
     * @param verticalAlignment vertical alignment ("Top", "Middle", "Bottom")
     * @return new Style instance with updated alignment properties
     */
    public Style withAlignment(String horizontalAlignment, String verticalAlignment) {
        return new Style(this.name, this.fontName, this.fontSize, this.isBold, this.fontColor, this.backColor,
                horizontalAlignment, verticalAlignment, this.padding, this.borderWidth, this.borderColor);
    }

    /**
     * Creates a new Style with modified border properties.
     *
     * @param width border width in points
     * @param color border color in hex format (e.g., "#CCCCCC") or null to keep current
     * @return new Style instance with updated border properties
     */
    public Style withBorders(float width, String color) {
        return new Style(this.name, this.fontName, this.fontSize, this.isBold, this.fontColor, this.backColor,
                this.horizontalAlignment, this.verticalAlignment, this.padding, width, color != null ? color : this.borderColor);
    }

    /**
     * Creates a new Style with modified padding.
     *
     * @param padding padding in pixels applied to all sides
     * @return new Style instance with updated padding
     */
    public Style withPadding(int padding) {
        return new Style(this.name, this.fontName, this.fontSize, this.isBold, this.fontColor, this.backColor,
                this.horizontalAlignment, this.verticalAlignment, padding, this.borderWidth, this.borderColor);
    }

    /**
     * Returns the unique name of the style.
     *
     * @return style name
     */
    public String getName() { return name; }

    /**
     * Returns the font family name.
     *
     * @return font family name (e.g., "DejaVu Sans")
     */
    public String getFontName() { return fontName; }

    /**
     * Returns the font size in points.
     *
     * @return font size
     */
    public float getFontSize() { return fontSize; }

    /**
     * Returns whether the font is bold.
     *
     * @return true if font is bold
     */
    public boolean isBold() { return isBold; }

    /**
     * Returns the font color in hex format.
     *
     * @return font color (e.g., "#000000")
     */
    public String getFontColor() { return fontColor; }

    /**
     * Returns the background color in hex format.
     *
     * @return background color (e.g., "#FFFFFF") or null for transparent
     */
    public String getBackColor() { return backColor; }

    /**
     * Returns the horizontal text alignment.
     *
     * @return horizontal alignment ("Left", "Center", "Right", "Justified")
     */
    public String getHorizontalAlignment() { return horizontalAlignment; }

    /**
     * Returns the vertical text alignment.
     *
     * @return vertical alignment ("Top", "Middle", "Bottom")
     */
    public String getVerticalAlignment() { return verticalAlignment; }

    /**
     * Returns the border width in points.
     *
     * @return border width
     */
    public float getBorderWidth() { return borderWidth; }

    /**
     * Returns the border color in hex format.
     *
     * @return border color (e.g., "#CCCCCC")
     */
    public String getBorderColor() { return borderColor; }

    /**
     * Returns the padding in pixels.
     *
     * @return padding value or null if not set
     */
    public Integer getPadding() { return padding; }
}