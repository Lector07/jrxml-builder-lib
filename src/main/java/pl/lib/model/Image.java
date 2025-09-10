package pl.lib.model;

/**
 * Represents an image element in a report.
 *
 * <p>This class defines an image that can be embedded in a report,
 * including its expression source and positioning properties.</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * Image logo = new Image(
 *     "$P{CompanyLogo}",    // expression for image source
 *     10,                  // x position
 *     10,                  // y position
 *     100,                 // width
 *     50                   // height
 * );
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see pl.lib.model.CompanyInfo
 */
public class Image {
    private final String expression;
    private final int x, y, width, height;

    /**
     * Creates a new Image with specified properties.
     *
     * @param expression JasperReports expression for the image source
     * @param x x-coordinate position in pixels
     * @param y y-coordinate position in pixels
     * @param width image width in pixels
     * @param height image height in pixels
     */
    public Image(String expression, int x, int y, int width, int height) {
        this.expression = expression;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the JasperReports expression for the image source.
     *
     * @return image expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Returns the x-coordinate position of the image.
     *
     * @return x position in pixels
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate position of the image.
     *
     * @return y position in pixels
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the width of the image.
     *
     * @return width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the image.
     *
     * @return height in pixels
     */
    public int getHeight() {
        return height;
    }
}