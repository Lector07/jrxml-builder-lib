package pl.lib.config;

import pl.lib.api.ReportBuilder;

import java.util.*;

public class ReportConfig {
    private final String title;
    private final List<ColumnDefinition> columns;
    private final List<GroupDefinition> groups;
    // Nowe pole dla konfiguracji subraportów
    private final Map<String, ReportConfig> subreportConfigs;

    private ReportConfig(Builder builder) {
        this.title = builder.title;
        this.columns = builder.columns;
        this.groups = builder.groups;
        this.subreportConfigs = builder.subreportConfigs;
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

    // Nowa metoda do pobrania konfiguracji subraportów
    public Map<String, ReportConfig> getSubreportConfigs() {
        return subreportConfigs;
    }

    public static class Builder {
        private String title = "";
        private List<ColumnDefinition> columns = new ArrayList<>();
        private List<GroupDefinition> groups = new ArrayList<>();
        // Nowe pole dla buildera
        private Map<String, ReportConfig> subreportConfigs = new HashMap<>();

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder addColumn(ColumnDefinition column) {
            columns.add(column);
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

        public ReportConfig build() {
            return new ReportConfig(this);
        }
    }
}
