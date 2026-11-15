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
        ParseResult result = parser.parse("Фільтр;FLT-01;320.50;4;Постачальник");

        assertTrue(result.item().isPresent());
        assertEquals(new InventoryItem("Фільтр", "FLT-01", 320.50, 4, "Постачальник"),
                result.item().orElseThrow());
        assertTrue(result.error().isEmpty());
    }

    @Test
    void skipsBlankLine() {
        ParseResult result = parser.parse("   ");

        assertTrue(result.item().isEmpty());
        assertTrue(result.error().isEmpty());
    }

    @Test
    void rejectsWrongNumberOfFields() {
        ParseResult result = parser.parse("Фільтр;FLT-01;320.50;4");

        assertTrue(result.item().isEmpty());
        assertTrue(result.error().orElseThrow().contains("5 полів"));
    }

    @Test
    void rejectsNonNumericPrice() {
        ParseResult result = parser.parse("Фільтр;FLT-01;дорого;4;Постачальник");

        assertTrue(result.error().orElseThrow().contains("числами"));
    }

    @Test
    void rejectsNegativeInventoryValues() {
        ParseResult result = parser.parse("Фільтр;FLT-01;320.50;-4;Постачальник");

        assertTrue(result.error().orElseThrow().contains("від'ємними"));
    }

    @Test
    void handlesUkrainianText() {
        InventoryItem item = parser.parse("Ремінь ГРМ;TIM-008;2100.00;7;АвтоПостач")
                .item().orElseThrow();

        assertEquals("Ремінь ГРМ", item.name());
        assertEquals("АвтоПостач", item.supplier());
    }

    @Test
    void calculatesStatsForInputWithoutValidRecords() throws IOException {
        Path input = tempDir.resolve("invalid.csv");
        Files.writeString(input, "Фільтр;FLT-01;дорого;4;Постачальник\n"
                + "Фільтр;FLT-02;320.50;-4;Постачальник", StandardCharsets.UTF_8);
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
    }
}
