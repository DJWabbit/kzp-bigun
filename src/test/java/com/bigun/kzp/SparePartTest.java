package com.bigun.kzp;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SparePartTest {
    @Test
    void constructsSparePartWithValidValues() {
        SparePart part = new SparePart("FLT-01", "Фільтр", 4, 320.50, "Постачальник");

        assertEquals("FLT-01", part.getSku());
        assertEquals("Фільтр", part.getName());
        assertEquals(4, part.getStock());
        assertEquals(320.50, part.getUnitPrice());
        assertEquals("Постачальник", part.getSupplier());
    }

    @Test
    void acceptsZeroPriceAndStock() {
        SparePart part = new SparePart("FLT-00", "Фільтр", 0, 0.0, "Постачальник");

        assertEquals(0, part.getStock());
        assertEquals(0.0, part.getUnitPrice());
    }

    @Test
    void rejectsNullTextFields() {
        assertThrows(NullPointerException.class,
                () -> new SparePart(null, "Деталь", 1, 1.0, "Постачальник"));
        assertThrows(NullPointerException.class,
                () -> new SparePart("SKU", null, 1, 1.0, "Постачальник"));
        assertThrows(NullPointerException.class,
                () -> new SparePart("SKU", "Деталь", 1, 1.0, null));
    }

    @Test
    void rejectsBlankTextFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("\t", "Деталь", 1, 1.0, "Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("SKU", " ", 1, 1.0, "Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("SKU", "Деталь", 1, 1.0, ""));
    }

    @Test
    void rejectsNegativeOrNonFinitePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("SKU", "Деталь", 1, -0.01, "Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("SKU", "Деталь", 1, Double.NaN, "Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("SKU", "Деталь", 1, Double.POSITIVE_INFINITY, "Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("SKU", "Деталь", 1, Double.NEGATIVE_INFINITY, "Постачальник"));
    }

    @Test
    void rejectsNegativeStock() {
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("SKU", "Деталь", -1, 1.0, "Постачальник"));
    }

    @Test
    void parsesCsvWithUkrainianTextAndZeroValues() {
        SparePart part = SparePart.fromCsv("TIM-008;Ремінь ГРМ;0;0;АвтоПостач");

        assertEquals("TIM-008", part.getSku());
        assertEquals("Ремінь ГРМ", part.getName());
        assertEquals(0, part.getStock());
        assertEquals(0.0, part.getUnitPrice());
        assertEquals("АвтоПостач", part.getSupplier());
    }

    @Test
    void trimsCsvFields() {
        SparePart part = SparePart.fromCsv(" FLT-01 ; Фільтр ; 4 ; 320.50 ; Постачальник ");

        assertEquals("FLT-01", part.getSku());
        assertEquals("Фільтр", part.getName());
        assertEquals(4, part.getStock());
        assertEquals(320.50, part.getUnitPrice());
        assertEquals("Постачальник", part.getSupplier());
    }

    @Test
    void rejectsNullCsvRow() {
        assertThrows(NullPointerException.class, () -> SparePart.fromCsv(null));
    }

    @Test
    void reportsReasonForEachMalformedCsvRow() {
        assertReason("порожній рядок", "  ");
        assertReason("очікується 5 полів, отримано 4", "SKU;Деталь;2;1");
        assertReason("очікується 5 полів, отримано 6", "SKU;Деталь;2;1;Постачальник;");
        assertReason("назва, артикул і постачальник не можуть бути порожніми", " ;Деталь;2;1;Постачальник");
        assertReason("ціна та кількість мають бути числами", "SKU;Деталь;2;не число;Постачальник");
        assertReason("ціна та кількість мають бути числами", "SKU;Деталь;2.5;1;Постачальник");
        assertReason("ціна має бути скінченним числом", "SKU;Деталь;2;NaN;Постачальник");
        assertReason("ціна та кількість не можуть бути від'ємними", "SKU;Деталь;2;-1;Постачальник");
        assertReason("ціна та кількість не можуть бути від'ємними", "SKU;Деталь;-2;1;Постачальник");
    }

    @Test
    void reportsBlankTextBeforeInvalidNumber() {
        assertReason("назва, артикул і постачальник не можуть бути порожніми", "SKU; ;не число;1;Постачальник");
    }

    @Test
    void keepsCauseOfInvalidNumber() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> SparePart.fromCsv("SKU;Деталь;два;1;Постачальник"));

        assertInstanceOf(NumberFormatException.class, exception.getCause());
    }

    @Test
    void formatsPriceUsingRootLocale() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            SparePart part = new SparePart("SKU", "Деталь", 2, 12.5, "Постачальник");

            assertEquals("SparePart{sku=\"SKU\", name=\"Деталь\", stock=2, unitPrice=12.50, "
                    + "supplier=\"Постачальник\"}", part.toString());
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    void exposesStockValueComponentsAndEquality() {
        StockValue value = new StockValue(12, 150.25);

        assertEquals(12, value.totalStock());
        assertEquals(150.25, value.totalValue());
        assertEquals(new StockValue(12, 150.25), value);
        assertEquals(new StockValue(12, 150.25).hashCode(), value.hashCode());
        assertNotEquals(new StockValue(13, 150.25), value);
    }

    @Test
    void validatesStockValueTotals() {
        assertEquals(0, new StockValue(0, 0.0).totalStock());
        assertThrows(IllegalArgumentException.class, () -> new StockValue(-1, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new StockValue(1, -0.01));
        assertThrows(IllegalArgumentException.class, () -> new StockValue(1, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new StockValue(1, Double.POSITIVE_INFINITY));
    }

    @Test
    void calculatesStockValueFromParts() {
        InventoryStats stats = InventoryStats.calculate(List.of(
                new SparePart("BRK-001", "Гальмівні колодки", 12, 1250.50, "АвтоПостач"),
                new SparePart("TIM-008", "Ремінь ГРМ", 7, 2100.00, "АвтоПостач")));

        assertEquals(19, stats.stockValue().totalStock());
        assertEquals(29706.00, stats.stockValue().totalValue(), 0.0001);
    }

    private static void assertReason(String expected, String line) {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> SparePart.fromCsv(line));
        assertEquals(expected, exception.getMessage());
    }
}
