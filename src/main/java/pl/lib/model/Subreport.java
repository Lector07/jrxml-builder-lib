package pl.lib.model;

import net.sf.jasperreports.engine.JasperReport;

public final class Subreport {
    private final String fieldName;
    private final JasperReport subreport;

    public Subreport(String fieldName, JasperReport subreport) {
        this.fieldName = fieldName;
        this.subreport = subreport;
    }

    public String getFieldName() {
        return fieldName;
    }

    public JasperReport getSubreport() {
        return subreport;
    }
}