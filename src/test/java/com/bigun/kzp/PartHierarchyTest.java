package com.bigun.kzp;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartHierarchyTest {
    private static final double DELTA = 0.0001;

    @Test
    void commonInvariantsHoldForEveryKind() {
        assertCommonInvariants(PartKind.ORIGINAL, "FLT-01");
        assertCommonInvariants(PartKind.REPLACEMENT, "FLT-01-R");
    }

    @Test
    void originalPartAcceptsValidValuesAndZeros() {
        SparePart part = new OriginalPart("BRK-001", "Колодки", 0, 0.0, "АвтоПостач");

        assertEquals("BRK-001", part.getSku());
        assertEquals(PartKind.ORIGINAL, part.getKind());
        assertEquals(0.0, part.warrantyCost(), DELTA);
    }

    @Test
    void originalPartRejectsReplacementSuffix() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new OriginalPart("BRK-001-R", "Колодки", 1, 1.0, "АвтоПостач"));

        assertEquals("Оригінальна деталь не може мати суфікс -R", exception.getMessage());
    }

    @Test
    void originalWarrantyIsFivePercent() {
        assertEquals(750.30, new OriginalPart("BRK-001", "Колодки", 12, 1250.50, "П").warrantyCost(), DELTA);
    }

    @Test
    void replacementPartAcceptsValidValuesAndZeros() {
        SparePart part = new ReplacementPart("FLT-014-R", "Фільтр", 0, 0.0, "Мотор-Сервіс");

        assertEquals("FLT-014-R", part.getSku());
        assertEquals(PartKind.REPLACEMENT, part.getKind());
        assertEquals(0.0, part.warrantyCost(), DELTA);
    }

    @Test
    void replacementPartRequiresSuffix() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ReplacementPart("FLT-014", "Фільтр", 1, 1.0, "Мотор-Сервіс"));

        assertEquals("Деталь-замінник повинна мати суфікс -R", exception.getMessage());
    }

    @Test
    void replacementWarrantyUsesMinimumForCheapParts() {
        assertEquals(500.00, replacement(25, 320.00).warrantyCost(), DELTA);
        assertEquals(20.00, replacement(1, 0.0).warrantyCost(), DELTA);
    }

    @Test
    void replacementWarrantyUsesThreePercentForExpensiveParts() {
        assertEquals(60.00, replacement(2, 1000.00).warrantyCost(), DELTA);
    }

    @Test
    void replacementWarrantyChangesAtTheBoundary() {
        double boundary = 20.0 / 0.03;

        assertEquals(20.00, replacement(1, boundary - 0.01).warrantyCost(), DELTA);
        assertEquals(20.00, replacement(1, boundary).warrantyCost(), DELTA);
        assertEquals(20.03, replacement(1, boundary + 1).warrantyCost(), DELTA);
    }

    @Test
    void kindHasLabelForReports() {
        assertEquals("оригінальна", PartKind.ORIGINAL.getLabel());
        assertEquals("замінник", PartKind.REPLACEMENT.getLabel());
    }

    @Test
    void kindIsRecognizedBySkuSuffix() {
        assertEquals(PartKind.REPLACEMENT, PartKind.fromSku("FLT-014-R"));
        assertEquals(PartKind.REPLACEMENT, PartKind.fromSku(" FLT-014-R "));
        assertEquals(PartKind.REPLACEMENT, PartKind.fromSku("-R"));
        assertEquals(PartKind.ORIGINAL, PartKind.fromSku("FLT-014"));
        assertEquals(PartKind.ORIGINAL, PartKind.fromSku("FLT-R-014"));
        assertEquals(PartKind.ORIGINAL, PartKind.fromSku("FLT-014-r"));
        assertEquals(PartKind.ORIGINAL, PartKind.fromSku("FLT014R"));
        assertEquals(PartKind.ORIGINAL, PartKind.fromSku(null));
    }

    @Test
    void kindCreatesMatchingSubtype() {
        assertInstanceOf(OriginalPart.class, PartKind.ORIGINAL.create("A-1", "Деталь", 1, 1.0, "П"));
        assertInstanceOf(ReplacementPart.class, PartKind.REPLACEMENT.create("A-1-R", "Деталь", 1, 1.0, "П"));
    }

    @Test
    void csvRowIsParsedToSubtypeBySku() {
        assertInstanceOf(OriginalPart.class, SparePart.fromCsv("FLT-014;Фільтр;25;320.00;Мотор-Сервіс"));
        assertInstanceOf(ReplacementPart.class, SparePart.fromCsv("FLT-014-R;Фільтр;25;320.00;Мотор-Сервіс"));
        assertInstanceOf(ReplacementPart.class, SparePart.fromCsv(" FLT-014-R ;Фільтр;25;320.00;Мотор-Сервіс"));
    }

    @Test
    void everyParsedRowAcceptedByPreviousWorkStaysValid() {
        assertEquals("-R", SparePart.fromCsv("-R;Фільтр;1;1;П").getSku());
    }

    @Test
    void subtypesCalculateWarrantyDifferentlyThroughBaseType() {
        SparePart original = new OriginalPart("A-1", "Деталь", 10, 100.00, "П");
        SparePart replacement = new ReplacementPart("A-1-R", "Деталь", 10, 100.00, "П");

        assertEquals(50.00, original.warrantyCost(), DELTA);
        assertEquals(200.00, replacement.warrantyCost(), DELTA);
        assertNotEquals(original.warrantyCost(), replacement.warrantyCost());
    }

    @Test
    void equalPartsHaveOneSetEntry() {
        SparePart first = new OriginalPart("A-1", "Деталь", 1, 1.0, "П");
        SparePart second = new OriginalPart("A-1", "Інша назва", 5, 9.0, "Р");
        Set<SparePart> parts = new HashSet<>(List.of(first));
        parts.add(second);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(1, parts.size());
    }

    @Test
    void differentSkuOrSubtypeIsNotEqual() {
        SparePart part = new OriginalPart("A-1", "Деталь", 1, 1.0, "П");
        SparePart replacement = new ReplacementPart("A-1-R", "Деталь", 1, 1.0, "П");

        assertNotEquals(part, new OriginalPart("A-2", "Деталь", 1, 1.0, "П"));
        assertNotEquals(part, replacement);
        assertNotEquals(null, part);
        assertNotEquals("A-1", part);
    }

    @Test
    void equalPartFindsHashMapValue() {
        Map<SparePart, String> places = new HashMap<>();
        places.put(new ReplacementPart("A-1-R", "Деталь", 1, 1.0, "П"), "стелаж 3");

        assertEquals("стелаж 3", places.get(new ReplacementPart("A-1-R", "Деталь", 7, 2.0, "П")));
    }

    @Test
    void warrantyReportListsPartsAndTotal() {
        List<SparePart> parts = List.of(
                new OriginalPart("BRK-001", "Колодки", 12, 1250.50, "АвтоПостач"),
                new ReplacementPart("FLT-014-R", "Фільтр", 25, 320.00, "Мотор-Сервіс"));

        String expected = String.format("Гарантійна вартість%n- BRK-001 (оригінальна): 750.30%n"
                + "- FLT-014-R (замінник): 500.00%nРазом: 1250.30%n");
        assertEquals(expected, WarrantyReport.format(parts));
    }

    @Test
    void warrantyReportHandlesEmptyList() {
        assertEquals(String.format("Гарантійна вартість%nРазом: 0.00%n"), WarrantyReport.format(List.of()));
    }

    @Test
    void warrantyOptionIsOffByDefault() {
        assertEquals(false, CliOptions.parse(new String[0]).warranty());
        assertEquals(true, CliOptions.parse(new String[]{"--warranty"}).warranty());
    }

    private static SparePart replacement(int stock, double price) {
        return new ReplacementPart("A-1-R", "Деталь", stock, price, "П");
    }

    private static void assertCommonInvariants(PartKind kind, String sku) {
        assertThrows(NullPointerException.class, () -> kind.create(null, "Деталь", 1, 1.0, "П"));
        assertThrows(NullPointerException.class, () -> kind.create(sku, null, 1, 1.0, "П"));
        assertThrows(IllegalArgumentException.class, () -> kind.create(" ", "Деталь", 1, 1.0, "П"));
        assertThrows(IllegalArgumentException.class, () -> kind.create(sku, " ", 1, 1.0, "П"));
        assertThrows(IllegalArgumentException.class, () -> kind.create(sku, "Деталь", -1, 1.0, "П"));
        assertThrows(IllegalArgumentException.class, () -> kind.create(sku, "Деталь", 1, -0.01, "П"));
        assertThrows(IllegalArgumentException.class, () -> kind.create(sku, "Деталь", 1, Double.NaN, "П"));
        assertThrows(IllegalArgumentException.class, () -> kind.create(sku, "Деталь", 1, 1.0, ""));
    }
}
