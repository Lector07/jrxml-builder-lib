package pl.lib.config;

import java.util.List;

/**
 * Report formatting options.
 *
 * <p>This class contains settings for visual report formatting,
 * including zebra stripes, highlighting rules, bookmark generation, etc.</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * FormattingOptions options = new FormattingOptions();
 * options.setZebraStripes(true);
 * options.setGenerateBookmarks(true);
 * options.setBookmarkField("name");
 *
 * List<HighlightRule> rules = Arrays.asList(
 *     new HighlightRule("price", "GREATER_THAN", "1000", "#FFCCCC")
 * );
 * options.setHighlightRules(rules);
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see HighlightRule
 */
public class FormattingOptions {
    private boolean zebraStripes;
    private boolean generateBookmarks;
    private List<HighlightRule> highlightRules;
    private String bookmarkField;

    /**
     * Checks if zebra stripes (alternating row colors) are enabled.
     *
     * @return true if zebra stripes are enabled
     */
    public boolean isZebraStripes() {
        return zebraStripes;
    }

    /**
     * Sets zebra stripes (alternating row colors).
     *
     * @param zebraStripes true to enable zebra stripes
     */
    public void setZebraStripes(boolean zebraStripes) {
        this.zebraStripes = zebraStripes;
    }

    /**
     * Checks if PDF bookmark generation is enabled.
     *
     * @return true if bookmarks should be generated
     */
    public boolean isGenerateBookmarks() {
        return generateBookmarks;
    }

    /**
     * Sets PDF bookmark generation.
     *
     * @param generateBookmarks true to enable bookmark generation
     */
    public void setGenerateBookmarks(boolean generateBookmarks) {
        this.generateBookmarks = generateBookmarks;
    }

    /**
     * Returns the list of cell highlighting rules.
     *
     * @return list of highlighting rules
     */
    public List<HighlightRule> getHighlightRules() {
        return highlightRules;
    }

    /**
     * Sets cell highlighting rules in the report.
     *
     * @param highlightRules list of highlighting rules
     */
    public void setHighlightRules(List<HighlightRule> highlightRules) {
        this.highlightRules = highlightRules;
    }

    /**
     * Returns the field name used for bookmark generation.
     *
     * @return field name for bookmarks
     */
    public String getBookmarkField() {
        return bookmarkField;
    }

    /**
     * Sets the field used for PDF bookmark generation.
     *
     * @param bookmarkField field name whose values will be used as bookmark names
     */
    public void setBookmarkField(String bookmarkField) {
        this.bookmarkField = bookmarkField;
    }
}