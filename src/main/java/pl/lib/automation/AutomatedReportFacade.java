package pl.lib.automation;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.component.ComponentKey;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.engine.type.*;
import pl.lib.api.ReportBuilder;
import pl.lib.model.CompanyInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import pl.lib.api.ReportBuilder;


public class AutomatedReportFacade {
    private final JsonReportGenerator jsonReportGenerator;
    private final ReportBuilder reportBuilder;


    public AutomatedReportFacade(){
        this.jsonReportGenerator = new JsonReportGenerator();
    }

    public AutomatedReportFacade(boolean printJrxml){
        this.jsonReportGenerator = new JsonReportGenerator().withJrxmlPrinting(printJrxml);
    }

    public byte[] generateCompositeReport(String jsonContent, String reportTitle, CompanyInfo companyInfo) throws JRException, IOException {
            JasperPrint mainContentPrint = jsonReportGenerator.generateReport(jsonContent, reportTitle);

            JasperPrint titlePagePrint = createTitlePage(reportTitle, companyInfo);

            JasperPrint tocPagePrint = createTocPage(mainContentPrint);

            List<JasperPrint> printList = new ArrayList<>();
            printList.add(titlePagePrint);
            printList.add(tocPagePrint);
            printList.add(mainContentPrint);

            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
    }
}
