package pl.lib.automation.page;

import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.*;
import pl.lib.model.ReportStyles;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TitlePageGenerator {

    public void addTitlePage(JasperDesign design, String reportTitle, String city) {
        int availableWidth = design.getColumnWidth();
        int pageHeight = design.getPageHeight() - design.getTopMargin() - design.getBottomMargin();

        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(pageHeight);
        titleBand.setSplitType(SplitTypeEnum.PREVENT);

        int currentY = 40;

        JRDesignStaticText logoPlaceholder = new JRDesignStaticText();
        logoPlaceholder.setX((availableWidth - 200) / 2);
        logoPlaceholder.setY(currentY);
        logoPlaceholder.setWidth(200);
        logoPlaceholder.setHeight(80);
        logoPlaceholder.setText("LOGO URZĘDU");
        logoPlaceholder.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        logoPlaceholder.setFontSize(14f);
        logoPlaceholder.setBold(true);
        logoPlaceholder.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        logoPlaceholder.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        logoPlaceholder.setForecolor(Color.decode("#999999"));
        logoPlaceholder.setMode(ModeEnum.OPAQUE);
        logoPlaceholder.setBackcolor(Color.decode("#F5F5F5"));
        titleBand.addElement(logoPlaceholder);

        currentY += 120;

        JRDesignTextField titleField = new JRDesignTextField();
        titleField.setX(20);
        titleField.setY(currentY);
        titleField.setWidth(availableWidth - 40);
        titleField.setHeight(60);
        titleField.setExpression(new JRDesignExpression("\"" + escapeQuotes(reportTitle) + "\""));
        titleField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        titleField.setFontSize(24f);
        titleField.setBold(true);
        titleField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        titleField.setForecolor(Color.decode("#2A3F54"));
        titleBand.addElement(titleField);

        // Stopka na dole strony
        int footerY = pageHeight - 30;

        String dateStr = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        String footerText = city + ", " + dateStr;

        JRDesignStaticText footerField = new JRDesignStaticText();
        footerField.setX(0);
        footerField.setY(footerY);
        footerField.setWidth(availableWidth);
        footerField.setHeight(20);
        footerField.setText(footerText);
        footerField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        footerField.setFontSize(12f);
        footerField.setBold(false);
        footerField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        footerField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        footerField.setForecolor(Color.decode("#666666"));
        titleBand.addElement(footerField);

        JRDesignBreak pageBreak = new JRDesignBreak();
        pageBreak.setType(BreakTypeEnum.PAGE);
        pageBreak.setX(0);
        pageBreak.setY(pageHeight - 1);
        pageBreak.setWidth(availableWidth);
        pageBreak.setHeight(1);
        titleBand.addElement(pageBreak);

        design.setTitle(titleBand);
    }

    private String escapeQuotes(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"");
    }
}

