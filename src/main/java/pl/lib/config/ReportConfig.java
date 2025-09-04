package pl.lib.config;

import pl.lib.model.CompanyInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportConfig {
    private String title;
    private List<ColumnDefinition> columns;
    private List<GroupDefinition> groups;
    private Map<String, ReportConfig> subreportConfigs;
    private CompanyInfo companyInfo;
    private boolean useSubreportBorders;

    private boolean pageFooterEnabled;
    private String footerLeftText;
    private String orientation;
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
    }

    public String getTitle() {
        return title;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public List<GroupDefinition> getGroups() {
        return groups;
    }

    public Map<String, ReportConfig> getSubreportConfigs() {
        return subreportConfigs;
    }

    public CompanyInfo getCompanyInfo() {
        return companyInfo;
    }

    public boolean isUseSubreportBorders() {
        return useSubreportBorders;
    }

    public boolean isPageFooterEnabled() {
        return pageFooterEnabled;
    }

    public String getFooterLeftText(){
        return footerLeftText;
    }

    public String getOrientation(){
        return orientation;
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

        public Builder title(String title) {
            this.title = title;
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

        public Builder addFooterLeftText(String footerLeftText){
            this.footerLeftText = footerLeftText;
            return this;
        }

        public ReportConfig build() {
            return new ReportConfig(this);
        }
    }
}