package pl.lib.automation.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import pl.lib.model.BudgetHierarchyNode;
import pl.lib.model.BudgetNodeType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BudgetStructureAnalyzer {

    public boolean isBudgetData(JsonNode node) {
        if (!node.isObject()) {
            return false;
        }

        boolean hasStructure = node.has("struktura") && node.get("struktura").isArray();
        boolean hasBudgetFields = node.has("plan") && node.has("wykonanie") && node.has("kod");

        return hasStructure || hasBudgetFields;
    }

    public BudgetHierarchyNode buildBudgetTree(JsonNode budgetData) {
        if (budgetData.has("struktura")) {
            return buildTreeFromStructure(budgetData.get("struktura"));
        } else if (budgetData.isArray()) {
            return buildTreeFromArray(budgetData);
        } else {
            return buildSingleNode(budgetData, 1);
        }
    }

    private BudgetHierarchyNode buildTreeFromStructure(JsonNode structure) {
        BudgetHierarchyNode root = new BudgetHierarchyNode("ROOT", "Budget Root", BudgetNodeType.SECTION, 0);

        if (structure.isArray()) {
            for (JsonNode item : structure) {
                BudgetHierarchyNode node = buildNodeRecursively(item, 1);
                if (node != null) {
                    root.addChild(node);
                }
            }
        }

        calculateAggregates(root);
        return root;
    }

    private BudgetHierarchyNode buildTreeFromArray(JsonNode array) {
        BudgetHierarchyNode root = new BudgetHierarchyNode("ROOT", "Budget Root", BudgetNodeType.SECTION, 0);

        for (JsonNode item : array) {
            BudgetHierarchyNode node = buildNodeRecursively(item, 1);
            if (node != null) {
                root.addChild(node);
            }
        }

        calculateAggregates(root);
        return root;
    }

    private BudgetHierarchyNode buildNodeRecursively(JsonNode node, int level) {
        if (!node.isObject()) {
            return null;
        }

        String code = node.has("kod") ? node.get("kod").asText() : "";
        String name = node.has("nazwa") ? node.get("nazwa").asText() : "";

        BudgetNodeType type = determineNodeType(level);
        BudgetHierarchyNode budgetNode = new BudgetHierarchyNode(code, name, type, level);

        if (node.has("plan")) {
            budgetNode.setPlannedAmount(new BigDecimal(node.get("plan").asText()));
        }

        if (node.has("wykonanie")) {
            budgetNode.setActualAmount(new BigDecimal(node.get("wykonanie").asText()));
        }

        if (node.has("dzieci") && node.get("dzieci").isArray()) {
            for (JsonNode child : node.get("dzieci")) {
                BudgetHierarchyNode childNode = buildNodeRecursively(child, level + 1);
                if (childNode != null) {
                    budgetNode.addChild(childNode);
                }
            }
        }

        return budgetNode;
    }

    private BudgetHierarchyNode buildSingleNode(JsonNode node, int level) {
        String code = node.has("kod") ? node.get("kod").asText() : "";
        String name = node.has("nazwa") ? node.get("nazwa").asText() : "Budget Item";

        BudgetNodeType type = determineNodeType(level);
        BudgetHierarchyNode budgetNode = new BudgetHierarchyNode(code, name, type, level);

        if (node.has("plan")) {
            budgetNode.setPlannedAmount(new BigDecimal(node.get("plan").asText()));
        }

        if (node.has("wykonanie")) {
            budgetNode.setActualAmount(new BigDecimal(node.get("wykonanie").asText()));
        }

        return budgetNode;
    }

    private BudgetNodeType determineNodeType(int level) {
        switch (level) {
            case 1:
                return BudgetNodeType.SECTION;
            case 2:
                return BudgetNodeType.CHAPTER;
            default:
                return BudgetNodeType.PARAGRAPH;
        }
    }

    public void calculateAggregates(BudgetHierarchyNode root) {
        if (root == null) {
            return;
        }

        calculateAggregatesRecursive(root);
    }

    private void calculateAggregatesRecursive(BudgetHierarchyNode node) {
        if (node.hasChildren()) {
            BigDecimal totalPlanned = BigDecimal.ZERO;
            BigDecimal totalActual = BigDecimal.ZERO;

            for (BudgetHierarchyNode child : node.getChildren()) {
                calculateAggregatesRecursive(child);
                totalPlanned = totalPlanned.add(child.getPlannedAmount() != null ? child.getPlannedAmount() : BigDecimal.ZERO);
                totalActual = totalActual.add(child.getActualAmount() != null ? child.getActualAmount() : BigDecimal.ZERO);
            }

            if (node.getPlannedAmount() == null || node.getPlannedAmount().compareTo(BigDecimal.ZERO) == 0) {
                node.setPlannedAmount(totalPlanned);
            }
            if (node.getActualAmount() == null || node.getActualAmount().compareTo(BigDecimal.ZERO) == 0) {
                node.setActualAmount(totalActual);
            }
        }
    }

    public List<BudgetHierarchyNode> flattenTree(BudgetHierarchyNode root) {
        List<BudgetHierarchyNode> flatList = new ArrayList<>();
        flattenTreeRecursive(root, flatList);
        return flatList;
    }

    private void flattenTreeRecursive(BudgetHierarchyNode node, List<BudgetHierarchyNode> flatList) {
        if (node == null) {
            return;
        }

        if (!"ROOT".equals(node.getCode())) {
            flatList.add(node);
        }

        for (BudgetHierarchyNode child : node.getChildren()) {
            flattenTreeRecursive(child, flatList);
        }
    }
}

