package pl.lib.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BudgetHierarchyNodeTest {

    @Test
    void shouldCalculateExecutionPercent() {
        BudgetHierarchyNode node = new BudgetHierarchyNode();
        node.setPlannedAmount(new BigDecimal("1000"));
        node.setActualAmount(new BigDecimal("850"));

        BigDecimal percent = node.getExecutionPercent();
        assertEquals(new BigDecimal("85.00"), percent);
    }

    @Test
    void shouldReturnZeroPercentWhenPlannedIsZero() {
        BudgetHierarchyNode node = new BudgetHierarchyNode();
        node.setPlannedAmount(BigDecimal.ZERO);
        node.setActualAmount(new BigDecimal("100"));

        BigDecimal percent = node.getExecutionPercent();
        assertEquals(BigDecimal.ZERO, percent);
    }

    @Test
    void shouldCalculateDifference() {
        BudgetHierarchyNode node = new BudgetHierarchyNode();
        node.setPlannedAmount(new BigDecimal("1000"));
        node.setActualAmount(new BigDecimal("850"));

        BigDecimal difference = node.getDifference();
        assertEquals(new BigDecimal("150"), difference);
    }

    @Test
    void shouldCalculateTotalPlannedWithChildren() {
        BudgetHierarchyNode parent = new BudgetHierarchyNode("750", "Admin", BudgetNodeType.SECTION, 1);
        parent.setPlannedAmount(new BigDecimal("1000"));

        BudgetHierarchyNode child1 = new BudgetHierarchyNode("75011", "Child1", BudgetNodeType.CHAPTER, 2);
        child1.setPlannedAmount(new BigDecimal("500"));

        BudgetHierarchyNode child2 = new BudgetHierarchyNode("75012", "Child2", BudgetNodeType.CHAPTER, 2);
        child2.setPlannedAmount(new BigDecimal("300"));

        parent.addChild(child1);
        parent.addChild(child2);

        BigDecimal total = parent.getTotalPlanned();
        assertEquals(new BigDecimal("1800"), total);
    }

    @Test
    void shouldCalculateTotalActualWithChildren() {
        BudgetHierarchyNode parent = new BudgetHierarchyNode("750", "Admin", BudgetNodeType.SECTION, 1);
        parent.setActualAmount(new BigDecimal("900"));

        BudgetHierarchyNode child1 = new BudgetHierarchyNode("75011", "Child1", BudgetNodeType.CHAPTER, 2);
        child1.setActualAmount(new BigDecimal("450"));

        BudgetHierarchyNode child2 = new BudgetHierarchyNode("75012", "Child2", BudgetNodeType.CHAPTER, 2);
        child2.setActualAmount(new BigDecimal("280"));

        parent.addChild(child1);
        parent.addChild(child2);

        BigDecimal total = parent.getTotalActual();
        assertEquals(new BigDecimal("1630"), total);
    }

    @Test
    void shouldHandleNullAmounts() {
        BudgetHierarchyNode node = new BudgetHierarchyNode();
        node.setPlannedAmount(null);
        node.setActualAmount(null);

        BigDecimal percent = node.getExecutionPercent();
        BigDecimal difference = node.getDifference();

        assertEquals(BigDecimal.ZERO, percent);
        assertEquals(BigDecimal.ZERO, difference);
    }

    @Test
    void shouldCheckHasChildren() {
        BudgetHierarchyNode parent = new BudgetHierarchyNode();
        assertFalse(parent.hasChildren());

        BudgetHierarchyNode child = new BudgetHierarchyNode();
        parent.addChild(child);
        assertTrue(parent.hasChildren());
    }

    @Test
    void shouldSetAndGetAllProperties() {
        BudgetHierarchyNode node = new BudgetHierarchyNode();
        node.setCode("750");
        node.setName("Administracja publiczna");
        node.setType(BudgetNodeType.SECTION);
        node.setLevel(1);
        node.setPlannedAmount(new BigDecimal("1000000"));
        node.setActualAmount(new BigDecimal("950000"));

        assertEquals("750", node.getCode());
        assertEquals("Administracja publiczna", node.getName());
        assertEquals(BudgetNodeType.SECTION, node.getType());
        assertEquals(1, node.getLevel());
        assertEquals(new BigDecimal("1000000"), node.getPlannedAmount());
        assertEquals(new BigDecimal("950000"), node.getActualAmount());
    }
}

