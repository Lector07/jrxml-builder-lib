package pl.lib.model;
import net.sf.jasperreports.engine.JasperReport;
public final class Subreport {
    private final String fieldName;
    private final JasperReport subreport;
    private final int height;
    private boolean showSummary;
    public Subreport(String fieldName, JasperReport subreport) {
        this(fieldName, subreport, 50, false);
    }
    public Subreport(String fieldName, JasperReport subreport, int height, boolean showSummary) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty");
        }
        if (subreport == null) {
            throw new IllegalArgumentException("Subreport cannot be null");
        }
        if (height < 1) {
            throw new IllegalArgumentException("Height must be at least 1 pixel");
        }
        this.fieldName = fieldName;
        this.subreport = subreport;
        this.height = height;
        this.showSummary = showSummary;
    }
    public String getFieldName() {
        return fieldName;
    }
    public JasperReport getSubreport() {
        return subreport;
    }
    public int getHeight() {
        return height;
    }
    public boolean isShowSummary() {
        return showSummary;
    }
}