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

        // Dodanie subtelnego paska w górnej części
        JRDesignRectangle headerBar = new JRDesignRectangle();
        headerBar.setX(0);
        headerBar.setY(0);
        headerBar.setWidth(availableWidth);
        headerBar.setHeight(5);
        headerBar.setBackcolor(Color.decode("#1C3A57"));
        headerBar.setMode(ModeEnum.OPAQUE);
        titleBand.addElement(headerBar);

        int currentY = 60;
        JRDesignImage logoImage = new JRDesignImage(design);
        logoImage.setX((availableWidth - 220) / 2);
        logoImage.setY(currentY);
        logoImage.setWidth(220);
        logoImage.setHeight(90);
        logoImage.setScaleImage(ScaleImageEnum.RETAIN_SHAPE);
        logoImage.setHorizontalImageAlign(HorizontalImageAlignEnum.CENTER);
        logoImage.setVerticalImageAlign(VerticalImageAlignEnum.MIDDLE);
        JRDesignExpression logoExpression = new JRDesignExpression();
        logoExpression.setText("\"pobrane.png\"");
        logoImage.setExpression(logoExpression);
        titleBand.addElement(logoImage);

        currentY += 130;

        // Tytuł z lepszym spacingiem
        JRDesignTextField titleField = new JRDesignTextField();
        titleField.setX(30);
        titleField.setY(currentY);
        titleField.setWidth(availableWidth - 60);
        titleField.setHeight(80);
        titleField.setExpression(new JRDesignExpression("\"" + escapeQuotes(reportTitle) + "\""));
        titleField.setFontName(ReportStyles.FONT_DEJAVU_SANS_CONDENSED);
        titleField.setFontSize(24f);
        titleField.setBold(true);
        titleField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        titleField.setForecolor(Color.decode("#1C3A57"));
        titleField.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        titleBand.addElement(titleField);

        // Elegancka stopka na dole
        int footerY = Math.max(titleBand.getHeight() - 60, currentY + 120);

        // Linia nad stopką
        JRDesignLine separatorLine = new JRDesignLine();
        separatorLine.setX(availableWidth / 4);
        separatorLine.setY(footerY - 10);
        separatorLine.setWidth(availableWidth / 2);
        separatorLine.setHeight(0);
        separatorLine.setForecolor(Color.decode("#BDC3C7"));
        titleBand.addElement(separatorLine);

        String dateStr = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        String footerText = city + " • " + dateStr;

        JRDesignStaticText footerField = new JRDesignStaticText();
        footerField.setX(0);
        footerField.setY(footerY + 10);
        footerField.setWidth(availableWidth);
        footerField.setHeight(25);
        footerField.setText(footerText);
        footerField.setFontName(ReportStyles.FONT_DEJAVU_SANS_CONDENSED);
        footerField.setFontSize(12f);
        footerField.setBold(false);
        footerField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        footerField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        footerField.setForecolor(Color.decode("#7F8C8D"));
        titleBand.addElement(footerField);

        design.setTitle(titleBand);
    }

    private String escapeQuotes(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"");
    }
}
