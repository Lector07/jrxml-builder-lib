package pl.lib.model;

import net.sf.jasperreports.engine.JasperReport;

public final class Subreport {
    private final String targetBand;
    private final JasperReport subreport;
    private final String dataSourceExpression;
    private final String subreportObjectParameterName;
    private final String dataSourceParameterName;

    public Subreport(String targetBand, JasperReport subreport, String dataSourceExpression) {
        this.targetBand = targetBand;
        this.subreport = subreport;
        this.dataSourceExpression = dataSourceExpression;
        this.dataSourceParameterName = null; // Domyślnie
        if (subreport != null && subreport.getName() != null) {
            this.subreportObjectParameterName = "SUBREPORT_OBJECT_" + subreport.getName();
        } else {
            this.subreportObjectParameterName = "SUBREPORT_OBJECT_UNKNOWN";
        }
    }

    private Subreport(String targetBand, JasperReport subreport, String dataSourceExpression, String subreportObjectParameterName, String dataSourceParameterName) {
        this.targetBand = targetBand;
        this.subreport = subreport;
        this.dataSourceExpression = dataSourceExpression;
        this.subreportObjectParameterName = subreportObjectParameterName;
        this.dataSourceParameterName = dataSourceParameterName;
    }

    public Subreport withSubreportObjectParameterName(String name) {
        return new Subreport(this.targetBand, this.subreport, this.dataSourceExpression, name, this.dataSourceParameterName);
    }

    public Subreport withDataSourceParameterName(String name) {
        return new Subreport(this.targetBand, this.subreport, this.dataSourceExpression, this.subreportObjectParameterName, name);
    }

    public String getTargetBand() { return targetBand; }
    public JasperReport getSubreport() { return subreport; }
    public String getDataSourceExpression() { return dataSourceExpression; }
    public String getSubreportObjectParameterName() { return subreportObjectParameterName; }
    public String getDataSourceParameterName() { return dataSourceParameterName; }
}