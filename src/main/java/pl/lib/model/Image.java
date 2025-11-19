package pl.lib.model;
public class Image {
    private final String expression;
    private final int x, y, width, height;
    public Image(String expression, int x, int y, int width, int height) {
        this.expression = expression;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public String getExpression() {
        return expression;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
}