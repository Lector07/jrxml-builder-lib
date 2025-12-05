package pl.lib.automation.compiler;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.*;
import pl.lib.automation.analyzer.BudgetStructureAnalyzer;
import pl.lib.automation.util.CurrencyFormatter;
import pl.lib.config.BudgetTableConfig;
import pl.lib.model.BudgetHierarchyNode;

import java.awt.Color;
import java.util.*;

public class BudgetTableCompiler {

    private final CurrencyFormatter currencyFormatter;
    private final BudgetStructureAnalyzer budgetAnalyzer;

    public BudgetTableCompiler() {
        this.currencyFormatter = CurrencyFormatter.forPLN();
        this.budgetAnalyzer = new BudgetStructureAnalyzer();
    }

    public JasperReport compileBudgetTable(BudgetHierarchyNode rootNode, BudgetTableConfig config, int availableWidth) throws JRException {
        JasperDesign design = createBudgetTableDesign(availableWidth);

        List<Map<String, Object>> flatData = convertTreeToFlatData(rootNode, config);

        addColumnsToDesign(design, config, availableWidth);
        addColumnHeaderBand(design, config, availableWidth);
        addDetailBand(design, config, availableWidth);

        JasperReport report = JasperCompileManager.compileReport(design);
        return report;
    }

    private JasperDesign createBudgetTableDesign(int width) throws JRException {
        JasperDesign design = new JasperDesign();
        design.setName("BudgetTable");
        design.setPageWidth(width + 40);
        design.setPageHeight(842);
        design.setColumnWidth(width);
        design.setColumnSpacing(0);
        design.setLeftMargin(20);
        design.setRightMargin(20);
        design.setTopMargin(0);
        design.setBottomMargin(0);
        design.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);

        return design;
    }

    private void addColumnsToDesign(JasperDesign design, BudgetTableConfig config, int width) throws JRException {
        JRDesignField codeField = new JRDesignField();
        codeField.setName("code");
        codeField.setValueClass(String.class);
        design.addField(codeField);

        JRDesignField nameField = new JRDesignField();
        nameField.setName("name");
        nameField.setValueClass(String.class);
        design.addField(nameField);

        JRDesignField indentField = new JRDesignField();
        indentField.setName("indent");
        indentField.setValueClass(Integer.class);
        design.addField(indentField);

        JRDesignField plannedField = new JRDesignField();
        plannedField.setName("planned");
        plannedField.setValueClass(String.class);
        design.addField(plannedField);

        JRDesignField actualField = new JRDesignField();
        actualField.setName("actual");
        actualField.setValueClass(String.class);
        design.addField(actualField);

        if (config.isShowPercentages()) {
            JRDesignField percentField = new JRDesignField();
            percentField.setName("percent");
            percentField.setValueClass(String.class);
            design.addField(percentField);
        }

        if (config.isShowDifferences()) {
            JRDesignField diffField = new JRDesignField();
            diffField.setName("difference");
            diffField.setValueClass(String.class);
            design.addField(diffField);
        }

        JRDesignField isBoldField = new JRDesignField();
        isBoldField.setName("isBold");
        isBoldField.setValueClass(Boolean.class);
        design.addField(isBoldField);
    }

    private void addColumnHeaderBand(JasperDesign design, BudgetTableConfig config, int width) throws JRException {
        JRDesignBand columnHeaderBand = new JRDesignBand();
        columnHeaderBand.setHeight(25);

        int x = 0;
        int headerHeight = 25;

        if (config.isShowCode()) {
            int codeWidth = 80;
            JRDesignStaticText codeHeader = createHeaderLabel("Kod", x, 0, codeWidth, headerHeight);
            columnHeaderBand.addElement(codeHeader);
            x += codeWidth;
        }

        int nameWidth = width - x - 360;
        if (!config.isShowPercentages()) nameWidth += 60;
        if (!config.isShowDifferences()) nameWidth += 100;

        JRDesignStaticText nameHeader = createHeaderLabel("Nazwa", x, 0, nameWidth, headerHeight);
        nameHeader.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        columnHeaderBand.addElement(nameHeader);
        x += nameWidth;

        int amountWidth = 100;
        JRDesignStaticText plannedHeader = createHeaderLabel("Plan", x, 0, amountWidth, headerHeight);
        columnHeaderBand.addElement(plannedHeader);
        x += amountWidth;

        JRDesignStaticText actualHeader = createHeaderLabel("Wykonanie", x, 0, amountWidth, headerHeight);
        columnHeaderBand.addElement(actualHeader);
        x += amountWidth;

        if (config.isShowPercentages()) {
            int percentWidth = 60;
            JRDesignStaticText percentHeader = createHeaderLabel("%", x, 0, percentWidth, headerHeight);
            columnHeaderBand.addElement(percentHeader);
            x += percentWidth;
        }

        if (config.isShowDifferences()) {
            int diffWidth = 100;
            JRDesignStaticText diffHeader = createHeaderLabel("Różnica", x, 0, diffWidth, headerHeight);
            columnHeaderBand.addElement(diffHeader);
        }

        design.setColumnHeader(columnHeaderBand);
    }

    private JRDesignStaticText createHeaderLabel(String text, int x, int y, int width, int height) {
        JRDesignStaticText staticText = new JRDesignStaticText();
        staticText.setX(x);
        staticText.setY(y);
        staticText.setWidth(width);
        staticText.setHeight(height);
        staticText.setText(text);
        staticText.setFontName("DejaVu Sans Condensed");
        staticText.setFontSize(9f);
        staticText.setBold(true);
        staticText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        staticText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        staticText.setMode(ModeEnum.OPAQUE);
        staticText.setBackcolor(new Color(232, 238, 244));
        staticText.setForecolor(new Color(28, 58, 87));

        staticText.getLineBox().getTopPen().setLineWidth(0.5f);
        staticText.getLineBox().getTopPen().setLineColor(new Color(176, 176, 176));
        staticText.getLineBox().getBottomPen().setLineWidth(0.5f);
        staticText.getLineBox().getBottomPen().setLineColor(new Color(176, 176, 176));
        staticText.getLineBox().getLeftPen().setLineWidth(0.5f);
        staticText.getLineBox().getLeftPen().setLineColor(new Color(176, 176, 176));
        staticText.getLineBox().getRightPen().setLineWidth(0.5f);
        staticText.getLineBox().getRightPen().setLineColor(new Color(176, 176, 176));

        return staticText;
    }

    private void addDetailBand(JasperDesign design, BudgetTableConfig config, int width) throws JRException {
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(20);

        int x = 0;
        int columnHeight = 20;

        if (config.isShowCode()) {
            int codeWidth = 80;
            JRDesignTextField codeField = createTextField("code", x, 0, codeWidth, columnHeight, design);
            codeField.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
            detailBand.addElement(codeField);
            x += codeWidth;
        }

        int nameWidth = width - x - 360;
        if (!config.isShowPercentages()) nameWidth += 60;
        if (!config.isShowDifferences()) nameWidth += 100;

        JRDesignTextField nameField = createTextField("name", x, 0, nameWidth, columnHeight, design);
        nameField.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        nameField.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        detailBand.addElement(nameField);
        x += nameWidth;

        int amountWidth = 100;
        JRDesignTextField plannedField = createTextField("planned", x, 0, amountWidth, columnHeight, design);
        plannedField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        detailBand.addElement(plannedField);
        x += amountWidth;

        JRDesignTextField actualField = createTextField("actual", x, 0, amountWidth, columnHeight, design);
        actualField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        detailBand.addElement(actualField);
        x += amountWidth;

        if (config.isShowPercentages()) {
            int percentWidth = 60;
            JRDesignTextField percentField = createTextField("percent", x, 0, percentWidth, columnHeight, design);
            percentField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
            detailBand.addElement(percentField);
            x += percentWidth;
        }

        if (config.isShowDifferences()) {
            int diffWidth = 100;
            JRDesignTextField diffField = createTextField("difference", x, 0, diffWidth, columnHeight, design);
            diffField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
            detailBand.addElement(diffField);
        }

        ((JRDesignSection) design.getDetailSection()).addBand(detailBand);
    }

    private JRDesignTextField createTextField(String fieldName, int x, int y, int width, int height, JasperDesign design) throws JRException {
        JRDesignTextField textField = new JRDesignTextField();
        textField.setX(x);
        textField.setY(y);
        textField.setWidth(width);
        textField.setHeight(height);

        JRDesignExpression expression = new JRDesignExpression();
        expression.setText("$F{" + fieldName + "}");
        textField.setExpression(expression);

        textField.setFontName("DejaVu Sans Condensed");
        textField.setFontSize(9f);

        // Utwórz styl z warunkiem pogrubienia
        JRDesignStyle style = new JRDesignStyle();
        style.setName("Style_" + fieldName + "_" + x + "_" + System.currentTimeMillis());
        style.setDefault(false);

        JRDesignConditionalStyle conditionalStyle = new JRDesignConditionalStyle();
        JRDesignExpression conditionExpression = new JRDesignExpression();
        conditionExpression.setText("$F{isBold}");
        conditionalStyle.setConditionExpression(conditionExpression);
        conditionalStyle.setBold(Boolean.TRUE);

        style.addConditionalStyle(conditionalStyle);
        design.addStyle(style);

        textField.setStyle(style);

        return textField;
    }

    private List<Map<String, Object>> convertTreeToFlatData(BudgetHierarchyNode rootNode, BudgetTableConfig config) {
        List<Map<String, Object>> flatData = new ArrayList<>();
        List<BudgetHierarchyNode> flatTree = budgetAnalyzer.flattenTree(rootNode);

        for (BudgetHierarchyNode node : flatTree) {
            Map<String, Object> row = new HashMap<>();

            row.put("code", node.getCode() != null ? node.getCode() : "");

            String nameWithIndent = generateIndentation(node.getLevel(), config) + node.getName();
            row.put("name", nameWithIndent);
            row.put("indent", node.getLevel());

            row.put("planned", currencyFormatter.formatAmountWithoutCurrency(node.getPlannedAmount()));
            row.put("actual", currencyFormatter.formatAmountWithoutCurrency(node.getActualAmount()));

            if (config.isShowPercentages()) {
                row.put("percent", currencyFormatter.formatPercentDirect(node.getExecutionPercent()));
            }

            if (config.isShowDifferences()) {
                row.put("difference", currencyFormatter.formatDifferenceWithoutCurrency(node.getDifference()));
            }

            boolean isBold = node.hasChildren() && config.isBoldSubtotals();
            row.put("isBold", isBold);

            flatData.add(row);
        }

        return flatData;
    }

    private String generateIndentation(int level, BudgetTableConfig config) {
        if (!config.isIndentHierarchy() || level <= 1) {
            return "";
        }

        int spaces = (level - 1) * config.getIndentSize();
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            indent.append(" ");
        }
        return indent.toString();
    }
}
