package pl.lib.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class BudgetHierarchyNode {

    private String code;
    private String name;
    private BudgetNodeType type;
    private BigDecimal plannedAmount;
    private BigDecimal actualAmount;
    private int level;
    private List<BudgetHierarchyNode> children;

    public BudgetHierarchyNode() {
        this.children = new ArrayList<>();
        this.plannedAmount = BigDecimal.ZERO;
        this.actualAmount = BigDecimal.ZERO;
    }

    public BudgetHierarchyNode(String code, String name, BudgetNodeType type, int level) {
        this();
        this.code = code;
        this.name = name;
        this.type = type;
        this.level = level;
    }

    public BigDecimal getExecutionPercent() {
        if (plannedAmount == null || plannedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (actualAmount == null) {
            return BigDecimal.ZERO;
        }
        return actualAmount.divide(plannedAmount, 4, RoundingMode.HALF_UP)
                          .multiply(new BigDecimal("100"))
                          .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDifference() {
        BigDecimal planned = plannedAmount != null ? plannedAmount : BigDecimal.ZERO;
        BigDecimal actual = actualAmount != null ? actualAmount : BigDecimal.ZERO;
        return planned.subtract(actual);
    }

    public BigDecimal getTotalPlanned() {
        BigDecimal total = plannedAmount != null ? plannedAmount : BigDecimal.ZERO;
        for (BudgetHierarchyNode child : children) {
            total = total.add(child.getTotalPlanned());
        }
        return total;
    }

    public BigDecimal getTotalActual() {
        BigDecimal total = actualAmount != null ? actualAmount : BigDecimal.ZERO;
        for (BudgetHierarchyNode child : children) {
            total = total.add(child.getTotalActual());
        }
        return total;
    }

    public void addChild(BudgetHierarchyNode child) {
        this.children.add(child);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BudgetNodeType getType() {
        return type;
    }

    public void setType(BudgetNodeType type) {
        this.type = type;
    }

    public BigDecimal getPlannedAmount() {
        return plannedAmount;
    }

    public void setPlannedAmount(BigDecimal plannedAmount) {
        this.plannedAmount = plannedAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<BudgetHierarchyNode> getChildren() {
        return children;
    }

    public void setChildren(List<BudgetHierarchyNode> children) {
        this.children = children;
    }
}

