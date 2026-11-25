package com.bigun.kzp;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartStreamQueriesTest {
    private static final double DELTA = 0.0001;

    private static final SparePart BRAKE = part("BRK-001;Гальмівні колодки;12;1250.50;АвтоПостач");
    private static final SparePart OIL_FILTER = part("FLT-014-R;Масляний фільтр;25;320.00;Мотор-Сервіс");
    private static final SparePart PLUG = part("SPK-210;Свічка запалювання;40;185.75;Деталь+");
    private static final SparePart BELT = part("TIM-008;Ремінь ГРМ;7;2100.00;АвтоПостач");
    private static final SparePart OIL = part("OIL-101-R;Олива моторна;10;780.00;Мотор-Сервіс");
    private static final SparePart SHOCK = part("AMT-330;Амортизатор;4;2100.00;Деталь+");
    private static final SparePart LAMP = part("LMP-055-R;Лампа фари;9;150.00;АвтоПостач");
    private static final SparePart AIR_FILTER = part("FLT-014-R;Повітряний фільтр;3;410.00;Деталь+");

    private static final List<SparePart> PARTS =
            List.of(BRAKE, OIL_FILTER, PLUG, BELT, OIL, SHOCK, LAMP, AIR_FILTER);

    @Test
    void lowStockKeepsPartsBelowThresholdInOrder() {
        assertSameParts(List.of(BELT, SHOCK, LAMP, AIR_FILTER), PartStreamQueries.findLowStock(PARTS, 10));
    }

    @Test
    void lowStockExcludesPartOnThreshold() {
        assertSameParts(List.of(LAMP), PartStreamQueries.findLowStock(List.of(LAMP, OIL, BRAKE), 10));
        assertSameParts(List.of(LAMP, OIL), PartStreamQueries.findLowStock(List.of(LAMP, OIL, BRAKE), 11));
    }

    @Test
    void lowStockCanBeEmpty() {
        assertTrue(PartStreamQueries.findLowStock(PARTS, 0).isEmpty());
        assertTrue(PartStreamQueries.findLowStock(List.of(), 10).isEmpty());
    }

    @Test
    void skusKeepOrderAndDuplicates() {
        assertEquals(List.of("BRK-001", "FLT-014-R", "SPK-210", "TIM-008", "OIL-101-R", "AMT-330",
                "LMP-055-R", "FLT-014-R"), PartStreamQueries.extractSkus(PARTS));
        assertTrue(PartStreamQueries.extractSkus(List.of()).isEmpty());
    }

    @Test
    void stockValueIsSummedForRepeatedSuppliers() {
        Map<String, Double> values = PartStreamQueries.stockValueBySupplier(PARTS);

        assertEquals(List.of("АвтоПостач", "Деталь+", "Мотор-Сервіс"), List.copyOf(values.keySet()));
        assertEquals(31056.00, values.get("АвтоПостач"), DELTA);
        assertEquals(17060.00, values.get("Деталь+"), DELTA);
        assertEquals(15800.00, values.get("Мотор-Сервіс"), DELTA);
    }

    @Test
    void suppliersAreCaseSensitiveAndEmptyInputGivesEmptyMap() {
        Map<String, Double> values = PartStreamQueries.stockValueBySupplier(
                List.of(part("A-1;Деталь;2;10;S1"), part("A-2;Деталь;3;10;s1")));

        assertEquals(Map.of("S1", 20.0, "s1", 30.0), values);
        assertTrue(PartStreamQueries.stockValueBySupplier(List.of()).isEmpty());
    }

    @Test
    void priceStatisticsDescribeAllParts() {
        DoubleSummaryStatistics statistics = PartStreamQueries.priceStatistics(PARTS);

        assertEquals(8, statistics.getCount());
        assertEquals(7296.25, statistics.getSum(), DELTA);
        assertEquals(912.03125, statistics.getAverage(), DELTA);
        assertEquals(150.00, statistics.getMin(), DELTA);
        assertEquals(2100.00, statistics.getMax(), DELTA);
    }

    @Test
    void priceStatisticsOfEmptyListHaveSpecialValues() {
        DoubleSummaryStatistics statistics = PartStreamQueries.priceStatistics(List.of());

        assertEquals(0, statistics.getCount());
        assertEquals(0.0, statistics.getSum());
        assertEquals(0.0, statistics.getAverage());
        assertEquals(Double.POSITIVE_INFINITY, statistics.getMin());
        assertEquals(Double.NEGATIVE_INFINITY, statistics.getMax());
    }

    @Test
    void topFiveIsSortedByPriceThenNameAndTruncated() {
        assertSameParts(List.of(SHOCK, BELT, BRAKE, OIL, AIR_FILTER),
                PartStreamQueries.topExpensiveParts(PARTS, 5));
    }

    @Test
    void equalPricesAreSortedByNameRegardlessOfInputOrder() {
        assertSameParts(List.of(SHOCK, BELT), PartStreamQueries.topExpensiveParts(List.of(BELT, SHOCK), 2));
        assertSameParts(List.of(SHOCK, BELT), PartStreamQueries.topExpensiveParts(List.of(SHOCK, BELT), 2));
    }

    @Test
    void topHandlesBoundaryLimits() {
        assertTrue(PartStreamQueries.topExpensiveParts(PARTS, 0).isEmpty());
        assertSameParts(List.of(SHOCK), PartStreamQueries.topExpensiveParts(PARTS, 1));
        assertEquals(8, PartStreamQueries.topExpensiveParts(PARTS, 8).size());
        assertEquals(8, PartStreamQueries.topExpensiveParts(PARTS, 100).size());
        assertTrue(PartStreamQueries.topExpensiveParts(List.of(), 5).isEmpty());
    }

    @Test
    void topRejectsNegativeLimit() {
        assertThrows(IllegalArgumentException.class, () -> PartStreamQueries.topExpensiveParts(PARTS, -1));
    }

    @Test
    void findsPartBySkuIgnoringSurroundingSpaces() {
        assertSame(BELT, PartStreamQueries.findBySku(PARTS, "TIM-008").orElseThrow());
        assertSame(BELT, PartStreamQueries.findBySku(PARTS, "  TIM-008 ").orElseThrow());
    }

    @Test
    void duplicateSkuGivesFirstPart() {
        assertSame(OIL_FILTER, PartStreamQueries.findBySku(PARTS, "FLT-014-R").orElseThrow());
    }

    @Test
    void searchIsCaseSensitiveBecauseSuffixDefinesKind() {
        SparePart original = part("X-1-r;Деталь;1;1;П");
        SparePart replacement = part("X-1-R;Деталь;1;1;П");
        List<SparePart> parts = List.of(original, replacement);

        assertSame(replacement, PartStreamQueries.findBySku(parts, "X-1-R").orElseThrow());
        assertSame(original, PartStreamQueries.findBySku(parts, "X-1-r").orElseThrow());
        assertTrue(PartStreamQueries.findBySku(parts, "x-1-r").isEmpty());
    }

    @Test
    void missingOrBlankSkuGivesEmptyOptional() {
        assertTrue(PartStreamQueries.findBySku(PARTS, "XXX").isEmpty());
        assertTrue(PartStreamQueries.findBySku(PARTS, null).isEmpty());
        assertTrue(PartStreamQueries.findBySku(PARTS, "  ").isEmpty());
        assertTrue(PartStreamQueries.findBySku(List.of(), "TIM-008").isEmpty());
    }

    @Test
    void queriesRejectNullList() {
        assertThrows(NullPointerException.class, () -> PartStreamQueries.findLowStock(null, 10));
        assertThrows(NullPointerException.class, () -> PartStreamQueries.extractSkus(null));
        assertThrows(NullPointerException.class, () -> PartStreamQueries.stockValueBySupplier(null));
        assertThrows(NullPointerException.class, () -> PartStreamQueries.priceStatistics(null));
        assertThrows(NullPointerException.class, () -> PartStreamQueries.topExpensiveParts(null, 5));
        assertThrows(NullPointerException.class, () -> PartStreamQueries.findBySku(null, "TIM-008"));
    }

    @Test
    void queriesDoNotChangeInputList() {
        List<SparePart> source = new ArrayList<>(PARTS);

        PartStreamQueries.findLowStock(source, 10);
        PartStreamQueries.extractSkus(source);
        PartStreamQueries.stockValueBySupplier(source);
        PartStreamQueries.priceStatistics(source);
        PartStreamQueries.topExpensiveParts(source, 5);
        PartStreamQueries.findBySku(source, "TIM-008");

        assertSameParts(PARTS, source);
    }

    @Test
    void listResultsAreUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> PartStreamQueries.findLowStock(PARTS, 10).remove(0));
        assertThrows(UnsupportedOperationException.class,
                () -> PartStreamQueries.extractSkus(PARTS).add("X"));
        assertThrows(UnsupportedOperationException.class,
                () -> PartStreamQueries.topExpensiveParts(PARTS, 5).remove(0));
    }

    private static SparePart part(String line) {
        return SparePart.fromCsv(line);
    }

    /* Порівнює саме об'єкти: equals вважає рівними деталі з однаковим артикулом. */
    private static void assertSameParts(List<SparePart> expected, List<SparePart> actual) {
        assertEquals(expected.size(), actual.size());
        for (int index = 0; index < expected.size(); index++) {
            assertSame(expected.get(index), actual.get(index));
        }
    }
}
