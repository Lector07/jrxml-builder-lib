package pl.lib.config;

import java.util.List;

public class FormattingOptions {
    private boolean zebraStripes;
    private boolean generateBookmarks;
    private List<HighlightRule> highlightRules;
    private String bookmarkField;

    public boolean isZebraStripes() {
        return zebraStripes;
    }

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

    public String getBookmarkField() {
        return bookmarkField;
    }

    public void setBookmarkField(String bookmarkField) {
        this.bookmarkField = bookmarkField;
    }
}