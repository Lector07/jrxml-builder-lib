package pl.lib.config;

public class BudgetTableConfig {

    private boolean showCode = true;
    private boolean showPercentages = true;
    private boolean showDifferences = true;
    private boolean indentHierarchy = true;
    private int indentSize = 15;
    private boolean boldSubtotals = true;
    private String currencySymbol = "zł";
    private boolean useThousandsSeparator = true;
    private int decimalPlaces = 2;

    public BudgetTableConfig() {
    }

    public boolean isShowCode() {
        return showCode;
    }

    public void setShowCode(boolean showCode) {
        this.showCode = showCode;
    }

    public boolean isShowPercentages() {
        return showPercentages;
    }

    public void setShowPercentages(boolean showPercentages) {
        this.showPercentages = showPercentages;
    }

    public boolean isShowDifferences() {
        return showDifferences;
    }

    public void setShowDifferences(boolean showDifferences) {
        this.showDifferences = showDifferences;
    }

    public boolean isIndentHierarchy() {
        return indentHierarchy;
    }

    public void setIndentHierarchy(boolean indentHierarchy) {
        this.indentHierarchy = indentHierarchy;
    }

    public int getIndentSize() {
        return indentSize;
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    public boolean isBoldSubtotals() {
        return boldSubtotals;
    }

    public void setBoldSubtotals(boolean boldSubtotals) {
        this.boldSubtotals = boldSubtotals;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public boolean isUseThousandsSeparator() {
        return useThousandsSeparator;
    }

    public void setUseThousandsSeparator(boolean useThousandsSeparator) {
        this.useThousandsSeparator = useThousandsSeparator;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public static BudgetTableConfig defaultConfig() {
        return new BudgetTableConfig();
    }

    public static BudgetTableConfig minimalConfig() {
        BudgetTableConfig config = new BudgetTableConfig();
        config.setShowCode(false);
        config.setShowDifferences(false);
        config.setIndentSize(10);
        return config;
    }
}

