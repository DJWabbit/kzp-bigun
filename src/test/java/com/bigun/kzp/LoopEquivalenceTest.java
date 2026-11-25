package com.bigun.kzp;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/* Порівнює потокові обчислення з цикловою реалізацією лабораторної роботи №3. */
class LoopEquivalenceTest {
    private static final List<SparePart> CONTROL = List.of(
            SparePart.fromCsv("BRK-001;Гальмівні колодки;12;1250.50;АвтоПостач"),
            SparePart.fromCsv("FLT-014-R;Масляний фільтр;25;320.00;Мотор-Сервіс"),
            SparePart.fromCsv("SPK-210;Свічка запалювання;40;185.75;Деталь+"),
            SparePart.fromCsv("TIM-008;Ремінь ГРМ;7;2100.00;АвтоПостач"));

    @Test
    void controlDataGiveSameResults() {
        assertSameAsLoop(CONTROL);
    }

    @Test
    void emptyDataGiveSameResults() {
        assertSameAsLoop(List.of());
    }

    @Test
    void boundaryDataGiveSameResults() {
        assertSameAsLoop(List.of(
                SparePart.fromCsv("A-1;Деталь;0;0;П"),
                SparePart.fromCsv("A-1-R;Деталь;2147483647;0.01;П"),
                SparePart.fromCsv("A-2;Деталь;2147483647;0.1;П"),
                SparePart.fromCsv("A-2;Деталь;1;0.1;П")));
    }

    @Test
    void generatedDataGiveSameReports() {
        Random random = new Random(4);
        for (int attempt = 0; attempt < 200; attempt++) {
            List<SparePart> parts = new ArrayList<>();
            for (int index = 0; index < 50; index++) {
                String sku = "S-" + index + (random.nextBoolean() ? "-R" : "");
                double price = Math.round(random.nextDouble() * 100_000) / 100.0;
                parts.add(PartKind.fromSku(sku).create(sku, "Деталь", random.nextInt(100), price, "П"));
            }
            assertSameAsLoop(parts);
        }
    }

    private static void assertSameAsLoop(List<SparePart> parts) {
        long stock = 0;
        double value = 0;
        StringBuilder warranty = new StringBuilder(String.format("Гарантійна вартість%n"));
        double total = 0;
        for (SparePart part : parts) {
            stock += part.getStock();
            value += part.getUnitPrice() * part.getStock();
            double cost = part.warrantyCost();
            total += cost;
            warranty.append(String.format(Locale.ROOT, "- %s (%s): %.2f%n",
                    part.getSku(), part.getKind().getLabel(), cost));
        }
        warranty.append(String.format(Locale.ROOT, "Разом: %.2f%n", total));

        InventoryStats stats = InventoryStats.calculate(parts);
        assertEquals(parts.size(), stats.validRecordCount());
        assertEquals(stock, stats.stockValue().totalStock());
        assertEquals(money(value), money(stats.stockValue().totalValue()));
        assertEquals(warranty.toString(), WarrantyReport.format(parts));
    }

    private static String money(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
