package pl.lib.automation;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import java.io.IOException;

public class AutomatedReportService {
    private final JsonReportGenerator generator = new JsonReportGenerator();

    public void generatePdfReportFromJson(String jsonContent, String reportTitle, String outputPath)
            throws JRException, IOException {
        JasperPrint jasperPrint = generator.generateReportFromJson(jsonContent, reportTitle);
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
    }

    public void generatePdfReportFromJsonWithJrxml(String jsonContent, String reportTitle, String outputPath)
            throws JRException, IOException {
        JsonReportGenerator generatorWithPrinting = new JsonReportGenerator().withJrxmlPrinting(true);
        JasperPrint jasperPrint = generatorWithPrinting.generateReportFromJson(jsonContent, reportTitle);
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
    }

    public JasperPrint generateReportFromJson(String jsonContent, String reportTitle)
            throws JRException, IOException {
        return generator.generateReportFromJson(jsonContent, reportTitle);
    }

    public JasperPrint generateReportFromJsonWithJrxml(String jsonContent, String reportTitle)
            throws JRException, IOException {
        JsonReportGenerator generatorWithPrinting = new JsonReportGenerator().withJrxmlPrinting(true);
        return generatorWithPrinting.generateReportFromJson(jsonContent, reportTitle);
    }
}