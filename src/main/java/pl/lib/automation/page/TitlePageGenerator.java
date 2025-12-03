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
        titleBand.setHeight(Math.max(pageHeight - 35, 200));
        titleBand.setSplitType(SplitTypeEnum.PREVENT);

        int currentY = 40;
        JRDesignImage logoImage = new JRDesignImage(design);
        logoImage.setX((availableWidth - 200) / 2);
        logoImage.setY(currentY);
        logoImage.setWidth(200);
        logoImage.setHeight(80);
        logoImage.setScaleImage(ScaleImageEnum.RETAIN_SHAPE);
        logoImage.setHorizontalImageAlign(HorizontalImageAlignEnum.CENTER);
        logoImage.setVerticalImageAlign(VerticalImageAlignEnum.MIDDLE);
        JRDesignExpression logoExpression = new JRDesignExpression();
        logoExpression.setText("\"pobrane.png\"");
        logoImage.setExpression(logoExpression);
        titleBand.addElement(logoImage);

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

        int footerY = Math.max(titleBand.getHeight() - 30, currentY + 100);
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

        design.setTitle(titleBand);
    }

    private String escapeQuotes(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"");
    }
}
