package pl.lib.automation;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import pl.lib.config.ReportConfig;
import java.io.IOException;
public class AutomatedReportService {
    private final JsonReportGenerator generator = new JsonReportGenerator();
    public void generatePdfReportFromJson(String jsonContent, ReportConfig config, String outputPath)
            throws JRException, IOException {
        JasperPrint jasperPrint = generator.generateTableReportFromJson(jsonContent, config);
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
    }
    public void generatePdfReportFromJsonWithJrxml(String jsonContent, ReportConfig config, String outputPath)
            throws JRException, IOException {
        JsonReportGenerator generatorWithPrinting = new JsonReportGenerator().withJrxmlPrinting(true);
        JasperPrint jasperPrint = generatorWithPrinting.generateTableReportFromJson(jsonContent, config);
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
    }
    public JasperPrint generateReportFromJson(String jsonContent, ReportConfig config)
            throws JRException, IOException {
        return generator.generateTableReportFromJson(jsonContent, config);
    }
}