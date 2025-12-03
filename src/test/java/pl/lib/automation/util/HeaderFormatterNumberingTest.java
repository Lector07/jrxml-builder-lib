package pl.lib.automation.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeaderFormatterNumberingTest {

    private HeaderFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new HeaderFormatter();
    }

    @Test
    void shouldFormatNumberedHeader() {
        List<Integer> path = Arrays.asList(1, 2, 3);
        String result = formatter.formatNumberedHeader("Tytuł", path);
        assertEquals("1.2.3. Tytuł", result);
    }

    @Test
    void shouldFormatSingleLevelHeader() {
        List<Integer> path = Arrays.asList(1);
        String result = formatter.formatNumberedHeader("Wprowadzenie", path);
        assertEquals("1. Wprowadzenie", result);
    }

    @Test
    void shouldGenerateSequentialNumbers() {
        String num1 = formatter.getNextNumber(1);
        String num2 = formatter.getNextNumber(1);
        String num3 = formatter.getNextNumber(1);

        assertEquals("1", num1);
        assertEquals("2", num2);
        assertEquals("3", num3);
    }

    @Test
    void shouldGenerateHierarchicalNumbers() {
        String num1 = formatter.getNextNumber(1);
        String num1_1 = formatter.getNextNumber(2);
        String num1_2 = formatter.getNextNumber(2);
        String num2 = formatter.getNextNumber(1);
        String num2_1 = formatter.getNextNumber(2);

        assertEquals("1", num1);
        assertEquals("1.1", num1_1);
        assertEquals("1.2", num1_2);
        assertEquals("2", num2);
        assertEquals("2.1", num2_1);
    }

    @Test
    void shouldResetDeeperLevels() {
        formatter.getNextNumber(1);
        formatter.getNextNumber(2);
        formatter.getNextNumber(3);

        String num = formatter.getNextNumber(1);
        assertEquals("2", num);

        String numSub = formatter.getNextNumber(2);
        assertEquals("2.1", numSub);
    }

    @Test
    void shouldResetAllCounters() {
        formatter.getNextNumber(1);
        formatter.getNextNumber(2);
        formatter.getNextNumber(3);

        formatter.resetCounters();

        String num = formatter.getNextNumber(1);
        assertEquals("1", num);
    }

    @Test
    void shouldHandleThreeLevelHierarchy() {
        String num1 = formatter.getNextNumber(1);
        String num1_1 = formatter.getNextNumber(2);
        String num1_1_1 = formatter.getNextNumber(3);
        String num1_1_2 = formatter.getNextNumber(3);
        String num1_2 = formatter.getNextNumber(2);
        String num1_2_1 = formatter.getNextNumber(3);

        assertEquals("1", num1);
        assertEquals("1.1", num1_1);
        assertEquals("1.1.1", num1_1_1);
        assertEquals("1.1.2", num1_1_2);
        assertEquals("1.2", num1_2);
        assertEquals("1.2.1", num1_2_1);
    }

    @Test
    void shouldGetCurrentNumberPath() {
        formatter.getNextNumber(1);
        formatter.getNextNumber(2);
        formatter.getNextNumber(3);

        List<Integer> path = formatter.getCurrentNumberPath(3);
        assertEquals(Arrays.asList(1, 1, 1), path);
    }
}

