package pl.lib.config;

import java.util.List;

/**
 * OSTATECZNA POPRAWNA WERSJA - Usunięto pole pageFooterEnabled
 */
public class FormattingOptions {
    private boolean zebraStripes;
    private boolean generateBookmarks;
    private List<HighlightRule> highlightRules;
    private String bookmarkField; // opcjonalne: nazwa pola kolumny do zakładek

    // Getters
    public boolean isZebraStripes() {
        return zebraStripes;
    }

    // Setters
    public void setZebraStripes(boolean zebraStripes) {
        this.zebraStripes = zebraStripes;
    }

    public boolean isGenerateBookmarks() {
        return generateBookmarks;
    }

    public void setGenerateBookmarks(boolean generateBookmarks) {
        this.generateBookmarks = generateBookmarks;
    }

    public List<HighlightRule> getHighlightRules() {
        return highlightRules;
    }

    public void setHighlightRules(List<HighlightRule> highlightRules) {
        this.highlightRules = highlightRules;
    }

    public String getBookmarkField() { return bookmarkField; }
    public void setBookmarkField(String bookmarkField) { this.bookmarkField = bookmarkField; }
}