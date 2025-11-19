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
    public ReportConfig() {
    }
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
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public List<ColumnDefinition> getColumns() {
        return columns;
    }
    public void setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
    }
    public List<GroupDefinition> getGroups() {
        return groups;
    }
    public void setGroups(List<GroupDefinition> groups) {
        this.groups = groups;
    }
    public ColorSettings getColorSettings() {
        return colorSettings;
    }
    public void setColorSettings(ColorSettings colorSettings) {
        this.colorSettings = colorSettings;
    }
    public Map<String, ReportConfig> getSubreportConfigs() {
        return subreportConfigs;
    }
    public void setSubreportConfigs(Map<String, ReportConfig> subreportConfigs) {
        this.subreportConfigs = subreportConfigs;
    }
    public String getTheme() {
        return theme;
    }
    public void setTheme(String theme) {
        this.theme = theme;
    }
    public CompanyInfo getCompanyInfo() {
        return companyInfo;
    }
    public void setCompanyInfo(CompanyInfo companyInfo) {
        this.companyInfo = companyInfo;
    }
    public boolean isUseSubreportBorders() {
        return useSubreportBorders;
    }
    public void setUseSubreportBorders(boolean useSubreportBorders) {
        this.useSubreportBorders = useSubreportBorders;
    }
    public List<Integer> getMargins() {
        return margins;
    }
    public void setMargins(List<Integer> margins) {
        this.margins = margins;
    }
    public boolean isPageFooterEnabled() {
        return pageFooterEnabled;
    }
    public void setPageFooterEnabled(boolean pageFooterEnabled) {
        this.pageFooterEnabled = pageFooterEnabled;
    }
    public String getFooterLeftText() {
        return footerLeftText;
    }
    public void setFooterLeftText(String footerLeftText) {
        this.footerLeftText = footerLeftText;
    }
    public String getOrientation() {
        return orientation;
    }
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }
    public FormattingOptions getFormattingOptions() {
        return formattingOptions;
    }
    public void setFormattingOptions(FormattingOptions formattingOptions) {
        this.formattingOptions = formattingOptions;
    }
    public boolean isSummaryBandEnabled() {
        return summaryBandEnabled;
    }
    public void setSummaryBandEnabled(boolean summaryBandEnabled) {
        this.summaryBandEnabled = summaryBandEnabled;
    }
    public String getPageFormat() {
        return pageFormat;
    }
    public void setPageFormat(String pageFormat) {
        this.pageFormat = pageFormat;
    }
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
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        public Builder colorSettings(ColorSettings settings) {
            this.colorSettings = settings;
            return this;
        }
        public Builder addColumn(ColumnDefinition column) {
            columns.add(column);
            return this;
        }
        public Builder withPageFooterEnabled(boolean enabled) {
            this.pageFooterEnabled = enabled;
            return this;
        }
        public Builder addGroup(GroupDefinition group) {
            groups.add(group);
            return this;
        }
        public Builder withSubreportConfig(String fieldName, ReportConfig config) {
            subreportConfigs.put(fieldName, config);
            return this;
        }
        public Builder companyInfo(CompanyInfo companyInfo) {
            this.companyInfo = companyInfo;
            return this;
        }
        public Builder addColumns(List<ColumnDefinition> columns) {
            this.columns.addAll(columns);
            return this;
        }
        public Builder withSubreportBorders(boolean borders) {
            this.subreportBorders = borders;
            return this;
        }
        public Builder addGroups(List<GroupDefinition> groups) {
            this.groups.addAll(groups);
            return this;
        }
        public Builder addFooterLeftText(String footerLeftText) {
            this.footerLeftText = footerLeftText;
            return this;
        }
        public Builder margins(List<Integer> margins) {
            this.margins = margins;
            return this;
        }
        public Builder addFormattingOption(FormattingOptions formattingOptions) {
            this.formattingOptions = formattingOptions;
            return this;
        }
        public Builder withSummaryBandEnabled(boolean enabled) {
            this.summaryBandEnabled = enabled;
            return this;
        }
        public Builder pageFormat(String pageFormat) {
            this.pageFormat = pageFormat;
            return this;
        }
        public ReportConfig build() {
            return new ReportConfig(this);
        }
    }
}