package pl.lib.model;

import java.awt.Color;

public final class ReportStyles {

    private ReportStyles() {
        // Private constructor to prevent instantiation
    }

    // Style Names
    public static final String HEADER_STYLE = "HeaderStyle";
    public static final String DATA_STYLE = "DataStyle";
    public static final String NUMERIC_STYLE = "NumericStyle";
    public static final String GROUP_STYLE_1 = "GroupStyle1";
    public static final String GROUP_STYLE_2 = "GroupStyle2";

    // Colors
    public static final String COLOR_WHITE = "#FFFFFF";
    public static final String COLOR_BLACK = "#000000";
    public static final String COLOR_PRIMARY_BACKGROUND = "#2A3F54";
    public static final String COLOR_SECONDARY_BACKGROUND = "#4F6A83";
    public static final String COLOR_TABLE_HEADER_BACKGROUND = "#C6D8E4";
    public static final String COLOR_TABLE_BORDER = "#CCCCCC";
    public static final String COLOR_GROUP_BACKGROUND = "#D0D8E0";
    public static final Color FOOTER_BACKGROUND_COLOR = new Color(224, 224, 224, 150);


    // Fonts
    public static final String FONT_DEJAVU_SANS = "DejaVu Sans";
    public static final String FONT_DEJAVU_SANS_CONDENSED = "DejaVu Sans Condensed";

    // Patterns
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String NUMERIC_PATTERN = "#,##0.00";
}
