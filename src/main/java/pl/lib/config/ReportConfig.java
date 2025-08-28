package pl.lib.config;

import pl.lib.api.ReportBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ReportConfig {
    private final String title;
    private final List<ColumnDefinition> columns;
    private final List<GroupDefinition> groups;

    private ReportConfig(Builder builder) {
        this.title = builder.title;
        this.columns = Collections.unmodifiableList(new ArrayList<>(builder.columns));
        this.groups = Collections.unmodifiableList(new ArrayList<>(builder.groups));

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

    public Builder builder(){
        return new Builder();
    }

    public static final class Builder{
        private String title;
        private final List<ColumnDefinition> columns = new ArrayList<>();
        private final List<GroupDefinition> groups = new ArrayList<>();

        public Builder title(String title){
            this.title = title;
            return this;
        }

        public Builder addColumn(ColumnDefinition column){
            this.columns.add(Objects.requireNonNull(column, "column"));
            return this;
        }

        public Builder addColumns(List<ColumnDefinition> columns){
            if(columns != null){
                columns.forEach(this::addColumn);
            }
            return this;
        }

        public Builder addGroup(GroupDefinition group){
            this.groups.add(Objects.requireNonNull(group, "group"));
            return this;
        }

        public Builder addGroups(List<GroupDefinition> groups){
            if(groups != null){
                groups.forEach(this::addGroup);
            }
            return this;
        }

        public ReportConfig build(){
            return new ReportConfig(this);
        }

    }
}
