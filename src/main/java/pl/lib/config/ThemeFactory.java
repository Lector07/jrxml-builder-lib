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
            case ELEGANT -> createElegantTheme();
            case DEFAULT -> createDefaultTheme();
        };
    }

    private static List<Style> createDefaultTheme() {
        List<Style> styles = new ArrayList<>();
        styles.add(new Style(ReportStyles.HEADER_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 9, true)
                .withColors("#FFFFFF", "#1C3A57")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0f, "#1C3A57")
                .withPadding(5));
        styles.add(new Style(ReportStyles.DATA_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, false)
                .withColors("#2C3E50", "#FFFFFF")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#E8EEF4")
                .withPadding(4));
        styles.add(new Style(ReportStyles.NUMERIC_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, false)
                .withColors("#2C3E50", "#FFFFFF")
                .withAlignment("RIGHT", "MIDDLE")
                .withBorders(0.5f, "#E8EEF4")
                .withPadding(4));
        styles.add(new Style(GROUP_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 9, true)
                .withColors("#1C3A57", "#E8F4FD")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#C0D6E8")
                .withPadding(4));
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
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 9, true)
                .withColors("#2C3E50", "#ECF0F1")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0.5f, "#BDC3C7")
                .withPadding(5));
        styles.add(new Style(ReportStyles.DATA_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, false)
                .withColors("#2C3E50", "#FFFFFF")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#ECF0F1")
                .withPadding(4));
        styles.add(new Style(ReportStyles.NUMERIC_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, false)
                .withColors("#2C3E50", "#FFFFFF")
                .withAlignment("RIGHT", "MIDDLE")
                .withBorders(0.5f, "#ECF0F1")
                .withPadding(4));
        styles.add(new Style(GROUP_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, true)
                .withColors("#34495E", "#ECF0F1")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#BDC3C7")
                .withPadding(4));
        return styles;
    }

    private static List<Style> createCorporateTheme() {
        List<Style> styles = new ArrayList<>();
        styles.add(new Style(ReportStyles.HEADER_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 9, true)
                .withColors("#1A237E", "#E8EAF6")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0.5f, "#9FA8DA")
                .withPadding(5));
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
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 9, true)
                .withColors("#333333", "#FAFAFA")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0f, "#FAFAFA")
                .withPadding(6));
        styles.add(new Style(ReportStyles.DATA_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, false)
                .withColors("#333333", "#FFFFFF")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.25f, "#F0F0F0")
                .withPadding(5));
        styles.add(new Style(ReportStyles.NUMERIC_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, false)
                .withColors("#333333", "#FFFFFF")
                .withAlignment("RIGHT", "MIDDLE")
                .withBorders(0.25f, "#F0F0F0")
                .withPadding(5));
        styles.add(new Style(GROUP_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 9, true)
                .withColors("#555555", "#F8F8F8")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.25f, "#E0E0E0")
                .withPadding(5));
        return styles;
    }

    private static List<Style> createElegantTheme() {
        List<Style> styles = new ArrayList<>();
        // Ciemny fioletowy tekst na jasnym tle dla premium wyglądu
        styles.add(new Style(ReportStyles.HEADER_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 9, true)
                .withColors("#5D4E6D", "#F8F6FA")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0.5f, "#D4C5E0")
                .withPadding(6));
        styles.add(new Style(ReportStyles.DATA_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, false)
                .withColors("#3E3E3E", "#FFFFFF")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#F0EDF5")
                .withPadding(5));
        styles.add(new Style(ReportStyles.NUMERIC_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 8, false)
                .withColors("#3E3E3E", "#FFFFFF")
                .withAlignment("RIGHT", "MIDDLE")
                .withBorders(0.5f, "#F0EDF5")
                .withPadding(5));
        styles.add(new Style(GROUP_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 9, true)
                .withColors("#5D4E6D", "#F8F6FA")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#D4C5E0")
                .withPadding(5));
        return styles;
    }
}

