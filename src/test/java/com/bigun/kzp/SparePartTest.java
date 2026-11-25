package com.bigun.kzp;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SparePartTest {
    @Test
    void constructsSparePartWithValidValues() {
        SparePart part = new SparePart("Фільтр", "FLT-01", 320.50, 4, "Постачальник");

        assertEquals("Фільтр", part.getName());
        assertEquals("FLT-01", part.getSku());
        assertEquals(320.50, part.getUnitPrice());
        assertEquals(4, part.getStock());
        assertEquals("Постачальник", part.getSupplier());
    }

    @Test
    void acceptsZeroPriceAndStock() {
        SparePart part = new SparePart("Фільтр", "FLT-00", 0.0, 0, "Постачальник");

        assertEquals(0.0, part.getUnitPrice());
        assertEquals(0, part.getStock());
    }

    @Test
    void rejectsNullTextFields() {
        assertThrows(NullPointerException.class,
                () -> new SparePart(null, "SKU", 1.0, 1, "Постачальник"));
        assertThrows(NullPointerException.class,
                () -> new SparePart("Деталь", null, 1.0, 1, "Постачальник"));
        assertThrows(NullPointerException.class,
                () -> new SparePart("Деталь", "SKU", 1.0, 1, null));
    }

    @Test
    void rejectsBlankTextFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart(" ", "SKU", 1.0, 1, "Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("Деталь", "\t", 1.0, 1, "Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("Деталь", "SKU", 1.0, 1, ""));
    }

    @Test
    void rejectsNegativeOrNonFinitePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("Деталь", "SKU", -0.01, 1, "Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("Деталь", "SKU", Double.NaN, 1, "Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("Деталь", "SKU", Double.POSITIVE_INFINITY, 1, "Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("Деталь", "SKU", Double.NEGATIVE_INFINITY, 1, "Постачальник"));
    }

    @Test
    void rejectsNegativeStock() {
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("Деталь", "SKU", 1.0, -1, "Постачальник"));
    }

    @Test
    void parsesCsvWithUkrainianTextAndZeroValues() {
        SparePart part = SparePart.fromCsv("Ремінь ГРМ;TIM-008;0;0;АвтоПостач");

        assertEquals("Ремінь ГРМ", part.getName());
        assertEquals("TIM-008", part.getSku());
        assertEquals(0.0, part.getUnitPrice());
        assertEquals(0, part.getStock());
        assertEquals("АвтоПостач", part.getSupplier());
    }

    @Test
    void rejectsMalformedCsvRows() {
        assertThrows(IllegalArgumentException.class,
                () -> SparePart.fromCsv("Деталь;SKU;1;2"));
        assertThrows(IllegalArgumentException.class,
                () -> SparePart.fromCsv(" ;SKU;1;2;Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> SparePart.fromCsv("Деталь;SKU;не число;2;Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> SparePart.fromCsv("Деталь;SKU;-1;2;Постачальник"));
        assertThrows(IllegalArgumentException.class,
                () -> SparePart.fromCsv("Деталь;SKU;1;-2;Постачальник"));
    }

    @Test
    void formatsPriceUsingRootLocale() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            SparePart part = new SparePart("Деталь", "SKU", 12.5, 2, "Постачальник");

            assertEquals("SparePart{name=\"Деталь\", sku=\"SKU\", unitPrice=12.50, stock=2, "
                    + "supplier=\"Постачальник\"}", part.toString());
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    void validatesStockValueTotals() {
        assertEquals(new StockValue(0, 0.0), new StockValue(0, 0.0));
        assertEquals(new StockValue(12, 150.25), new StockValue(12, 150.25));
        assertThrows(IllegalArgumentException.class, () -> new StockValue(-1, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new StockValue(1, -0.01));
        assertThrows(IllegalArgumentException.class, () -> new StockValue(1, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new StockValue(1, Double.POSITIVE_INFINITY));
    }
}
