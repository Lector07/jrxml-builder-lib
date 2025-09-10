package pl.lib.model;

import java.awt.Color;

/**
 * Utility class containing predefined style names, colors, fonts and patterns for reports.
 *
 * <p>This class provides constants for standard report styling including
 * style names, color definitions, font families, and formatting patterns.
 * All constants are used throughout the library to ensure consistent appearance.</p>
 *
 * <h3>Style names:</h3>
 * <ul>
 *   <li>{@link #HEADER_STYLE} - Style for column headers</li>
 *   <li>{@link #DATA_STYLE} - Style for data cells</li>
 *   <li>{@link #NUMERIC_STYLE} - Style for numeric data cells</li>
 *   <li>{@link #GROUP_STYLE_1} - Primary group header style</li>
 *   <li>{@link #GROUP_STYLE_2} - Secondary group header style</li>
 * </ul>
 *
 * <h3>Color constants:</h3>
 * <ul>
 *   <li>Text colors: {@link #COLOR_WHITE}, {@link #COLOR_BLACK}</li>
 *   <li>Background colors: {@link #COLOR_PRIMARY_BACKGROUND}, {@link #COLOR_SECONDARY_BACKGROUND}</li>
 *   <li>Table colors: {@link #COLOR_TABLE_HEADER_BACKGROUND}, {@link #COLOR_TABLE_BORDER}</li>
 *   <li>Group colors: {@link #COLOR_GROUP_BACKGROUND}</li>
 * </ul>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * Style headerStyle = new Style(ReportStyles.HEADER_STYLE)
 *     .withFont(ReportStyles.FONT_DEJAVU_SANS, 10, true)
 *     .withColors(ReportStyles.COLOR_BLACK, ReportStyles.COLOR_TABLE_HEADER_BACKGROUND);
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see Style
 * @see pl.lib.api.ReportBuilder
 */
public final class ReportStyles {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ReportStyles() {
    }

    /** Style name for column headers */
    public static final String HEADER_STYLE = "HeaderStyle";

    /** Style name for regular data cells */
    public static final String DATA_STYLE = "DataStyle";

    /** Style name for numeric data cells */
    public static final String NUMERIC_STYLE = "NumericStyle";

    /** Primary group header style name */
    public static final String GROUP_STYLE_1 = "GroupStyle1";

    /** Secondary group header style name */
    public static final String GROUP_STYLE_2 = "GroupStyle2";

    /** White color constant */
    public static final String COLOR_WHITE = "#000000";

    /** Black color constant */
    public static final String COLOR_BLACK = "#000000";

    /** Primary background color (dark blue) */
    public static final String COLOR_PRIMARY_BACKGROUND = "#2A3F54";

    /** Secondary background color (medium blue) */
    public static final String COLOR_SECONDARY_BACKGROUND = "#4F6A83";

    /** Table header background color (light blue) */
    public static final String COLOR_TABLE_HEADER_BACKGROUND = "#C6D8E4";

    /** Table border color (light gray) */
    public static final String COLOR_TABLE_BORDER = "#CCCCCC";

    /** Group background color (light blue-gray) */
    public static final String COLOR_GROUP_BACKGROUND = "#D0D8E0";

    /** Footer background color with transparency */
    public static final Color FOOTER_BACKGROUND_COLOR = new Color(224, 224, 224, 150);

    /** DejaVu Sans font family name */
    public static final String FONT_DEJAVU_SANS = "DejaVu Sans";

    /** DejaVu Sans Condensed font family name */
    public static final String FONT_DEJAVU_SANS_CONDENSED = "DejaVu Sans Condensed";

    /** Default date formatting pattern */
    public static final String DATE_PATTERN = "dd/MM/yyyy";

    /** Default numeric formatting pattern */
    public static final String NUMERIC_PATTERN = "#,##0.00";
}
