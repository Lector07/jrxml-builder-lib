package pl.lib.automation.compiler;

import net.sf.jasperreports.engine.JRChart;
import org.junit.jupiter.api.Test;

/**
 * Test diagnostyczny do sprawdzenia wartości stałych typów wykresów
 */
class ChartTypeConstantsTest {

    @Test
    void printChartTypeConstants() {
        System.out.println("JRChart.CHART_TYPE_PIE = " + JRChart.CHART_TYPE_PIE);
        System.out.println("JRChart.CHART_TYPE_PIE3D = " + JRChart.CHART_TYPE_PIE3D);
        System.out.println("JRChart.CHART_TYPE_BAR = " + JRChart.CHART_TYPE_BAR);
        System.out.println("JRChart.CHART_TYPE_BAR3D = " + JRChart.CHART_TYPE_BAR3D);
        System.out.println("JRChart.CHART_TYPE_LINE = " + JRChart.CHART_TYPE_LINE);
        System.out.println("JRChart.CHART_TYPE_AREA = " + JRChart.CHART_TYPE_AREA);
        System.out.println("JRChart.CHART_TYPE_STACKEDBAR = " + JRChart.CHART_TYPE_STACKEDBAR);
        System.out.println("JRChart.CHART_TYPE_STACKEDBAR3D = " + JRChart.CHART_TYPE_STACKEDBAR3D);
    }
}

