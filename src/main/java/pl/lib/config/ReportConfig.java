package pl.lib.config;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pl.lib.model.ColorSettings;
import pl.lib.model.CompanyInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom deserializer for subreportConfigs that handles empty arrays as empty maps
 */
import com.fasterxml.jackson.databind.JavaType;

class SubreportConfigsDeserializer extends JsonDeserializer<Map<String, ReportConfig>> {
    @Override
    public Map<String, ReportConfig> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.getCurrentToken() == JsonToken.START_ARRAY) {
            p.nextToken();
            if (p.getCurrentToken() == JsonToken.END_ARRAY) {
                return new HashMap<>();
            }
            throw new IOException("Oczekiwano pustej tablicy [] dla subreportConfigs, ale tablica zawiera elementy");
        }

        JavaType mapType = ctxt.getTypeFactory().constructMapType(HashMap.class, String.class, ReportConfig.class);

        return ctxt.readValue(p, mapType);
    }
}

/**
 * Main report configuration class.
 *
 * <p>Contains all settings needed to generate a report, including
 * column definitions, groups, subreports, company information and formatting options.</p>
 *
 * <p>Uses Builder pattern for convenient creation of instances with optional fields.
 * Also supports JSON deserialization through appropriate setters.</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * ReportConfig config = new ReportConfig.Builder()
 *     .title("Sales Report")
 *     .addColumn(ColumnDefinition.builder("product").header("Product").build())
 *     .addColumn(ColumnDefinition.builder("price").header("Price").format("#,##0.00").build())
 *     .addGroup(GroupDefinition.builder("category").label("Category: ").build())
 *     .companyInfo(CompanyInfo.builder("My Company").build())
 *     .withPageFooterEnabled(true)
 *     .addFooterLeftText("Report generated automatically")
 *     .build();
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see ColumnDefinition
 * @see GroupDefinition
 * @see CompanyInfo
 * @see FormattingOptions
 */
public class ReportConfig {
    private String title;
    private List<ColumnDefinition> columns;
    private List<GroupDefinition> groups;
    @JsonDeserialize(using = SubreportConfigsDeserializer.class)
    private Map<String, ReportConfig> subreportConfigs;
    private CompanyInfo companyInfo;
    private boolean useSubreportBorders;
    private List<Integer> margins;
    private boolean pageFooterEnabled;
    private String footerLeftText;
    private String orientation;
    private boolean summaryBandEnabled = false;
    private String theme;
    private String pageFormat;
    private ColorSettings colorSettings;

    @JsonSetter(nulls = Nulls.SKIP)
    private FormattingOptions formattingOptions = new FormattingOptions();

    /**
     * Default constructor for JSON deserialization.
     */
    public ReportConfig() {
    }

    /**
     * Private constructor used by Builder.
     *
     * @param builder builder instance with set values
     */
    private ReportConfig(Builder builder) {
        this.title = builder.title;
        this.columns = builder.columns;
        this.groups = builder.groups;
        this.subreportConfigs = builder.subreportConfigs;
        this.companyInfo = builder.companyInfo;
        this.useSubreportBorders = builder.subreportBorders;
        this.pageFooterEnabled = builder.pageFooterEnabled;
        this.footerLeftText = builder.footerLeftText;
        this.orientation = builder.orientation;
        this.margins = builder.margins;
        this.summaryBandEnabled = builder.summaryBandEnabled;
        if (builder.formattingOptions != null) {
            this.formattingOptions = builder.formattingOptions;
        }
        this.pageFormat = builder.pageFormat;
        this.colorSettings = builder.colorSettings;
    }

    /**
     * Returns the report title.
     *
     * @return report title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the list of report column definitions.
     *
     * @return list of columns
     */
    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    /**
     * Returns the list of report group definitions.
     *
     * @return list of groups
     */
    public List<GroupDefinition> getGroups() {
        return groups;
    }

    public ColorSettings getColorSettings() {
        return colorSettings;
    }

    public void setColorSettings(ColorSettings colorSettings) {
        this.colorSettings = colorSettings;
    }

    /**
     * Returns the map of subreport configurations.
     *
     * <p>Key is the field name, value is the subreport configuration.</p>
     *
     * @return map of subreport configurations
     */
    public Map<String, ReportConfig> getSubreportConfigs() {
        return subreportConfigs;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * Returns company information used in the report.
     *
     * @return company information
     */
    public CompanyInfo getCompanyInfo() {
        return companyInfo;
    }

    /**
     * Checks if subreports should use borders.
     *
     * @return true if subreports have borders
     */
    public boolean isUseSubreportBorders() {
        return useSubreportBorders;
    }

    /**
     * Returns the list of report margins [top, right, bottom, left].
     *
     * @return list of margins in pixels
     */
    public List<Integer> getMargins() {
        return margins;
    }

    /**
     * Checks if page footer is enabled.
     *
     * @return true if page footer is enabled
     */
    public boolean isPageFooterEnabled() {
        return pageFooterEnabled;
    }

    /**
     * Returns the text displayed in the left part of the footer.
     *
     * @return left footer text
     */
    public String getFooterLeftText(){
        return footerLeftText;
    }

    /**
     * Returns the report orientation.
     *
     * @return orientation ("PORTRAIT" or "LANDSCAPE")
     */
    public String getOrientation(){
        return orientation;
    }

    /**
     * Returns the report formatting options.
     *
     * @return formatting options
     */
    public FormattingOptions getFormattingOptions() {
        return formattingOptions;
    }

    /**
     * Checks if summary band is enabled.
     *
     * @return true if summary band is enabled
     */
    public boolean isSummaryBandEnabled() {
        return summaryBandEnabled;
    }

    // Setters for JSON deserialization

    /**
     * Sets the report title.
     *
     * @param title report title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the list of column definitions.
     *
     * @param columns list of columns
     */
    public void setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
    }

    /**
     * Sets the list of group definitions.
     *
     * @param groups list of groups
     */
    public void setGroups(List<GroupDefinition> groups) {
        this.groups = groups;
    }

    /**
     * Sets the map of subreport configurations.
     *
     * @param subreportConfigs map of subreport configurations
     */
    public void setSubreportConfigs(Map<String, ReportConfig> subreportConfigs) {
        this.subreportConfigs = subreportConfigs;
    }

    /**
     * Sets company information.
     *
     * @param companyInfo company information
     */
    public void setCompanyInfo(CompanyInfo companyInfo) {
        this.companyInfo = companyInfo;
    }

    /**
     * Sets whether subreports should use borders.
     *
     * @param useSubreportBorders true to use borders
     */
    public void setUseSubreportBorders(boolean useSubreportBorders) {
        this.useSubreportBorders = useSubreportBorders;
    }

    /**
     * Sets report margins.
     *
     * @param margins list of margins [top, right, bottom, left]
     */
    public void setMargins(List<Integer> margins) {
        this.margins = margins;
    }

    /**
     * Sets whether page footer should be enabled.
     *
     * @param pageFooterEnabled true to enable footer
     */
    public void setPageFooterEnabled(boolean pageFooterEnabled) {
        this.pageFooterEnabled = pageFooterEnabled;
    }

    /**
     * Sets the left footer text.
     *
     * @param footerLeftText footer text
     */
    public void setFooterLeftText(String footerLeftText) {
        this.footerLeftText = footerLeftText;
    }

    /**
     * Sets the report orientation.
     *
     * @param orientation orientation ("PORTRAIT" or "LANDSCAPE")
     */
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getPageFormat() {
        return pageFormat;
    }

    public void setPageFormat(String pageFormat) {
        this.pageFormat = pageFormat;
    }

    /**
     * Sets the report formatting options.
     *
     * @param formattingOptions formatting options
     */
    public void setFormattingOptions(FormattingOptions formattingOptions) {
        this.formattingOptions = formattingOptions;
    }

    /**
     * Sets whether summary band should be enabled.
     *
     * @param summaryBandEnabled true to enable summary band
     */
    public void setSummaryBandEnabled(boolean summaryBandEnabled) {
        this.summaryBandEnabled = summaryBandEnabled;
    }

    /**
     * Builder for ReportConfig class implementing Builder pattern.
     *
     * <p>Enables step-by-step building of ReportConfig object with optional fields.</p>
     */
    public static class Builder {
        private String title = "";
        private List<ColumnDefinition> columns = new ArrayList<>();
        private List<GroupDefinition> groups = new ArrayList<>();
        private Map<String, ReportConfig> subreportConfigs = new HashMap<>();
        private CompanyInfo companyInfo;
        private boolean subreportBorders = false;
        private boolean pageFooterEnabled = true;
        private String footerLeftText = "";
        private String orientation = "PORTRAIT";
        private FormattingOptions formattingOptions;
        private List<Integer> margins;
        private boolean summaryBandEnabled = false;
        private String pageFormat = "A4";
        private ColorSettings colorSettings;

        /**
         * Sets the report title.
         *
         * @param title report title
         * @return this Builder (for method chaining)
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder colorSettings(ColorSettings settings) { this.colorSettings = settings; return this; }


        /**
         * Adds a column definition to the report.
         *
         * @param column column definition
         * @return this Builder (for method chaining)
         */
        public Builder addColumn(ColumnDefinition column) {
            columns.add(column);
            return this;
        }

        /**
         * Sets whether page footer should be enabled.
         *
         * @param enabled true to enable footer
         * @return this Builder (for method chaining)
         */
        public Builder withPageFooterEnabled(boolean enabled) {
            this.pageFooterEnabled = enabled;
            return this;
        }

        /**
         * Adds a group definition to the report.
         *
         * @param group group definition
         * @return this Builder (for method chaining)
         */
        public Builder addGroup(GroupDefinition group) {
            groups.add(group);
            return this;
        }

        /**
         * Adds a subreport configuration.
         *
         * @param fieldName field name for the subreport
         * @param config subreport configuration
         * @return this Builder (for method chaining)
         */
        public Builder withSubreportConfig(String fieldName, ReportConfig config) {
            subreportConfigs.put(fieldName, config);
            return this;
        }

        /**
         * Sets company information.
         *
         * @param companyInfo company information
         * @return this Builder (for method chaining)
         */
        public Builder companyInfo(CompanyInfo companyInfo) {
            this.companyInfo = companyInfo;
            return this;
        }

        /**
         * Adds a list of columns to the report.
         *
         * @param columns list of columns to add
         * @return this Builder (for method chaining)
         */
        public Builder addColumns(List<ColumnDefinition> columns) {
            this.columns.addAll(columns);
            return this;
        }

        /**
         * Sets whether subreports should use borders.
         *
         * @param borders true to use borders
         * @return this Builder (for method chaining)
         */
        public Builder withSubreportBorders(boolean borders) {
            this.subreportBorders = borders;
            return this;
        }

        /**
         * Adds a list of groups to the report.
         *
         * @param groups list of groups to add
         * @return this Builder (for method chaining)
         */
        public Builder addGroups(List<GroupDefinition> groups) {
            this.groups.addAll(groups);
            return this;
        }

        /**
         * Sets the left footer text.
         *
         * @param footerLeftText footer text
         * @return this Builder (for method chaining)
         */
        public Builder addFooterLeftText(String footerLeftText){
            this.footerLeftText = footerLeftText;
            return this;
        }

        /**
         * Sets report margins.
         *
         * @param margins list of margins [top, right, bottom, left]
         * @return this Builder (for method chaining)
         */
        public Builder margins(List<Integer> margins) {
            this.margins = margins;
            return this;
        }

        /**
         * Sets report formatting options.
         *
         * @param formattingOptions formatting options
         * @return this Builder (for method chaining)
         */
        public Builder addFormattingOption(FormattingOptions formattingOptions){
            this.formattingOptions = formattingOptions;
            return this;
        }

        /**
         * Sets whether summary band should be enabled.
         *
         * @param enabled true to enable summary band
         * @return this Builder (for method chaining)
         */
        public Builder withSummaryBandEnabled(boolean enabled) {
            this.summaryBandEnabled = enabled;
            return this;
        }

        public Builder pageFormat(String pageFormat) {
            this.pageFormat = pageFormat;
            return this;
        }

        /**
         * Builds the final ReportConfig instance.
         *
         * @return new ReportConfig instance with set values
         */
        public ReportConfig build() {
            return new ReportConfig(this);
        }
    }
}