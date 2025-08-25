package pl.lib.model;

import net.sf.jasperreports.engine.JasperReport;

public class Subreport {
    private final String targetBand;
    private final JasperReport compiledSubreport;
    private final String dataSourceParameterName;

    public Subreport(String targetBand, JasperReport compiledSubreport, String dataSourceParameterName) {
        this.targetBand = targetBand;
        this.compiledSubreport = compiledSubreport;
        this.dataSourceParameterName = dataSourceParameterName;
    }

    public String getTargetBand() {
        return targetBand;
    }

    public JasperReport getCompiledSubreport() {
        return compiledSubreport;
    }


    public String getDataSourceExpression() {
        return "$P{" + this.dataSourceParameterName + "}";
    }


    public String getSubreportObjectParameterName() {
        return "SUBREPORT_OBJECT_" + this.dataSourceParameterName;
    }
}