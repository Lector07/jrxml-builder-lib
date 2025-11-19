package pl.lib.config;
import pl.lib.model.Style;
import pl.lib.model.ReportStyles;
import java.util.ArrayList;
import java.util.List;
public class ThemeFactory {
    private static final String GROUP_STYLE = ReportStyles.GROUP_STYLE_1;
    public static List<Style> createStylesForTheme(ReportTheme theme) {
        return switch (theme) {
            case CLASSIC -> createClassicTheme();
            case MODERN -> createModernTheme();
            case CORPORATE -> createCorporateTheme();
            case MINIMAL -> createMinimalTheme();
            case DEFAULT -> createDefaultTheme();
        };
    }
    private static List<Style> createDefaultTheme() {
        List<Style> styles = new ArrayList<>();
        styles.add(new Style(ReportStyles.HEADER_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, true)
                .withColors("#000000", "#E3E3E3")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0.5f, "#E0E0E0")
                .withPadding(2));
        styles.add(new Style(ReportStyles.DATA_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false)
                .withColors("#000000", "#FFFFFF")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.25f, "#E0E0E0")
                .withPadding(2));
        styles.add(new Style(ReportStyles.NUMERIC_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false)
                .withColors("#000000", "#FFFFFF")
                .withAlignment("RIGHT", "MIDDLE")
                .withBorders(0.25f, "#E0E0E0")
                .withPadding(2));
        styles.add(new Style(GROUP_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, true)
                .withColors("#1E2A38", "#E8F4FD")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#C0D6E8")
                .withPadding(3));
        return styles;
    }
    private static List<Style> createClassicTheme() {
        List<Style> styles = new ArrayList<>();
        styles.add(new Style(ReportStyles.HEADER_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, true)
                .withColors("#000000", "#C6D8E4")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0.5f, "#CCCCCC")
                .withPadding(2));
        styles.add(new Style(ReportStyles.DATA_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false)
                .withColors("#000000", "#FFFFFF")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.25f, "#CCCCCC")
                .withPadding(2));
        styles.add(new Style(ReportStyles.NUMERIC_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false)
                .withColors("#000000", "#FFFFFF")
                .withAlignment("RIGHT", "MIDDLE")
                .withBorders(0.25f, "#CCCCCC")
                .withPadding(2));
        styles.add(new Style(GROUP_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, true)
                .withColors("#000000", "#EDEDED")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#CCCCCC")
                .withPadding(3));
        return styles;
    }
    private static List<Style> createModernTheme() {
        List<Style> styles = new ArrayList<>();
        styles.add(new Style(ReportStyles.HEADER_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, true)
                .withColors("#FFFFFF", "#4CAF50")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0.5f, "#388E3C")
                .withPadding(2));
        styles.add(new Style(ReportStyles.DATA_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false)
                .withColors("#2E7D32", "#FFFFFF")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.25f, "#C8E6C9")
                .withPadding(2));
        styles.add(new Style(ReportStyles.NUMERIC_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false)
                .withColors("#2E7D32", "#FFFFFF")
                .withAlignment("RIGHT", "MIDDLE")
                .withBorders(0.25f, "#C8E6C9")
                .withPadding(2));
        styles.add(new Style(GROUP_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, true)
                .withColors("#1B5E20", "#E8F5E8")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#81C784")
                .withPadding(3));
        return styles;
    }
    private static List<Style> createCorporateTheme() {
        List<Style> styles = new ArrayList<>();
        styles.add(new Style(ReportStyles.HEADER_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, true)
                .withColors("#FFFFFF", "#1A237E")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0.5f, "#0D1754")
                .withPadding(2));
        styles.add(new Style(ReportStyles.DATA_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false)
                .withColors("#1A237E", "#FFFFFF")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.25f, "#C5CAE9")
                .withPadding(2));
        styles.add(new Style(ReportStyles.NUMERIC_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false)
                .withColors("#1A237E", "#FFFFFF")
                .withAlignment("RIGHT", "MIDDLE")
                .withBorders(0.25f, "#C5CAE9")
                .withPadding(2));
        styles.add(new Style(GROUP_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, true)
                .withColors("#0D1754", "#E8EAF6")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#9FA8DA")
                .withPadding(3));
        return styles;
    }
    private static List<Style> createMinimalTheme() {
        List<Style> styles = new ArrayList<>();
        styles.add(new Style(ReportStyles.HEADER_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, true)
                .withColors("#FFFFFF", "#616161")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0.5f, "#424242")
                .withPadding(2));
        styles.add(new Style(ReportStyles.DATA_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false)
                .withColors("#212121", "#FFFFFF")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.25f, "#E0E0E0")
                .withPadding(2));
        styles.add(new Style(ReportStyles.NUMERIC_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false)
                .withColors("#212121", "#FFFFFF")
                .withAlignment("RIGHT", "MIDDLE")
                .withBorders(0.25f, "#E0E0E0")
                .withPadding(2));
        styles.add(new Style(GROUP_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, true)
                .withColors("#424242", "#F5F5F5")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#BDBDBD")
                .withPadding(3));
        return styles;
    }
}
