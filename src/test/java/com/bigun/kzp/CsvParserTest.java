package com.bigun.kzp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvParserTest {
    private final CsvParser parser = new CsvParser();

    @TempDir
    Path tempDir;

    @Test
    void parsesValidCsvLine() {
        ParseResult result = parser.parse("FLT-01;Фільтр;4;320.50;Постачальник");

        assertTrue(result.item().isPresent());
        assertEquals(new InventoryItem("FLT-01", "Фільтр", 4, 320.50, "Постачальник"),
                result.item().orElseThrow());
        assertTrue(result.error().isEmpty());
    }

    @Test
    void rejectsBlankLine() {
        ParseResult result = parser.parse("   ");

        assertTrue(result.item().isEmpty());
        assertTrue(result.error().orElseThrow().contains("порожній рядок"));
    }

    @Test
    void rejectsWrongNumberOfFields() {
        ParseResult result = parser.parse("FLT-01;Фільтр;4;320.50");

        assertTrue(result.item().isEmpty());
        assertTrue(result.error().orElseThrow().contains("5 полів"));
    }

    @Test
    void rejectsNonNumericPrice() {
        ParseResult result = parser.parse("FLT-01;Фільтр;4;дорого;Постачальник");

        assertTrue(result.error().orElseThrow().contains("числами"));
    }

    @Test
    void rejectsNegativeInventoryValues() {
        ParseResult result = parser.parse("FLT-01;Фільтр;-4;320.50;Постачальник");

        assertTrue(result.error().orElseThrow().contains("від'ємними"));
    }

    @Test
    void handlesUkrainianText() {
        InventoryItem item = parser.parse("TIM-008;Ремінь ГРМ;7;2100.00;АвтоПостач")
                .item().orElseThrow();

        assertEquals("Ремінь ГРМ", item.name());
        assertEquals("АвтоПостач", item.supplier());
    }

    @Test
    void calculatesStatsForInputWithoutValidRecords() throws IOException {
        Path input = tempDir.resolve("invalid.csv");
        Files.writeString(input, "FLT-01;Фільтр;4;дорого;Постачальник\n"
                + "FLT-02;Фільтр;-4;320.50;Постачальник", StandardCharsets.UTF_8);
        InventoryFileReader.ReadResult result = new InventoryFileReader().read(input);
        InventoryStats stats = InventoryStats.calculate(result.items());
        String report = new ReportFormatter().format(stats, result.errors());

        assertEquals(0, stats.validRecordCount());
        assertEquals(0, stats.totalStock());
        assertEquals(0.0, stats.totalValue());
        assertTrue(stats.mostExpensive().isEmpty());
        assertEquals(2, result.errors().size());
        assertTrue(result.errors().get(0).startsWith("Рядок 1:"));
        assertTrue(result.errors().get(1).startsWith("Рядок 2:"));
        assertTrue(report.contains("Найдорожча деталь: немає коректних записів"));
    }

    @Test
    void rejectsLegacyFieldOrder() {
        ParseResult result = parser.parse("Фільтр;FLT-01;320.50;4;Постачальник");

        assertTrue(result.item().isEmpty());
        assertTrue(result.error().orElseThrow().contains("числами"));
    }

    @Test
    void rejectsBlankTextField() {
        ParseResult result = parser.parse("FLT-01; ;4;320.50;Постачальник");

        assertTrue(result.item().isEmpty());
        assertTrue(result.error().orElseThrow().contains("порожніми"));
    }

    @Test
    void calculatesAllIndicatorsForSeveralRecords() {
        InventoryItem brake = new InventoryItem("BRK-001", "Гальмівні колодки", 12, 1250.50, "АвтоПостач");
        InventoryItem belt = new InventoryItem("TIM-008", "Ремінь ГРМ", 7, 2100.00, "АвтоПостач");

        InventoryStats stats = InventoryStats.calculate(List.of(brake, belt));

        assertEquals(2, stats.validRecordCount());
        assertEquals(19, stats.totalStock());
        assertEquals(29706.00, stats.totalValue(), 0.0001);
        assertEquals(belt, stats.mostExpensive().orElseThrow());
    }

    @Test
    void reportsBlankLineWithItsNumber() throws IOException {
        Path input = tempDir.resolve("blank.csv");
        Files.writeString(input, "\n \n", StandardCharsets.UTF_8);

        InventoryFileReader.ReadResult result = new InventoryFileReader().read(input);

        assertTrue(result.items().isEmpty());
        assertEquals(List.of("Рядок 1: порожній рядок", "Рядок 2: порожній рядок"), result.errors());
    }

    @Test
    void printsErrorsAsUtf8RegardlessOfStreamCharset() {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (PrintStream replacement = new PrintStream(captured, true, StandardCharsets.US_ASCII)) {
            System.setErr(replacement);
            Main.printError("Невідомий аргумент: --bad");
        } finally {
            System.setErr(originalErr);
        }

        assertEquals("Помилка: Невідомий аргумент: --bad", captured.toString(StandardCharsets.UTF_8).trim());
    }

    @Test
    void versionCommandPrintsVersionAndBuild() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (PrintStream replacement = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            System.setOut(replacement);
            Main.main(new String[]{"--version"});
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString(StandardCharsets.UTF_8).trim();
        assertTrue(output.matches("\\d+\\.\\d+\\.\\d+\\+\\w+"), output);
        assertFalse(output.contains("version="));
        assertFalse(output.contains("${"), output);
    }
}
