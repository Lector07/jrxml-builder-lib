package pl.lib.model;

import net.sf.jasperreports.engine.JasperReport;

public class Subreport {
    private final String targetBand;
    private final JasperReport subreport;
    private final String dataSourceExpression;

    private String subreportObjectParameterName;
    private String dataSourceParameterName;

    public Subreport(String targetBand, JasperReport subreport, String dataSourceExpression) {
        this.targetBand = targetBand;
        this.subreport = subreport;
        this.dataSourceExpression = dataSourceExpression;
        // Domyślna nazwa parametru dla obiektu podraportu jest generowana na podstawie nazwy raportu
        if (subreport != null && subreport.getName() != null) {
            this.subreportObjectParameterName = "SUBREPORT_OBJECT_" + subreport.getName();
        }
    }

    // --- Gettery używane przez ReportBuilder ---
    public String getTargetBand() {
        return targetBand;
    }

    public JasperReport getSubreport() {
        return subreport;
    }

    public String getDataSourceExpression() {
        return dataSourceExpression;
    }

    public String getSubreportObjectParameterName() {
        return subreportObjectParameterName;
    }

    public String getDataSourceParameterName() {
        return dataSourceParameterName;
    }

    // --- Fluent Settery (metody "with"), które brakowały ---
    public Subreport withSubreportObjectParameterName(String name) {
        this.subreportObjectParameterName = name;
        return this;
    }

    public Subreport withDataSourceParameterName(String name) {
        this.dataSourceParameterName = name;
        return this;
    }
}