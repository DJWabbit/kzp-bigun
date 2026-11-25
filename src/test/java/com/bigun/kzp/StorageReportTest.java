package com.bigun.kzp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageReportTest {
    @TempDir
    Path tempDir;

    @Test
    void exportsPartsAndConfirmsRoundTrip() throws Exception {
        List<SparePart> parts = new InventoryFileReader().read(Path.of("data", "input.csv")).items();
        Path file = tempDir.resolve("stock.csv");

        assertEquals(String.format("Експорт: записів 4%nRound-trip: так%n"), StorageReport.exportParts(file, parts));
        assertEquals(5, Files.readAllLines(file, StandardCharsets.UTF_8).size());
    }

    @Test
    void exportsEmptyList() throws DataStorageException {
        assertEquals(String.format("Експорт: записів 0%nRound-trip: так%n"),
                StorageReport.exportParts(tempDir.resolve("empty.csv"), List.of()));
    }

    @Test
    void importsPartsWithKinds() throws DataStorageException {
        assertEquals(String.format("Імпорт: записів 6%n- BRK-001 (оригінальна)%n- FLT-014-R (замінник)%n"
                + "- SPK-210 (оригінальна)%n- TIM-008 (оригінальна)%n- OIL-101-R (замінник)%n"
                + "- AMT-330 (оригінальна)%n"), StorageReport.importParts(Path.of("data", "stock.csv")));
    }

    @Test
    void importsFileWithoutRecords() throws DataStorageException {
        assertEquals(String.format("Імпорт: записів 0%n"),
                StorageReport.importParts(Path.of("data", "stock-empty.csv")));
    }

    @Test
    void importReportsWrongRecord() {
        assertThrows(DataStorageException.class,
                () -> StorageReport.importParts(Path.of("data", "stock-invalid.csv")));
    }

    @Test
    void errorDescriptionNamesFileErrorOnly() {
        DataStorageException missing = assertThrows(DataStorageException.class,
                () -> StorageReport.importParts(tempDir.resolve("missing.csv")));
        DataStorageException invalid = assertThrows(DataStorageException.class,
                () -> StorageReport.importParts(Path.of("data", "stock-invalid.csv")));

        assertTrue(Main.describe(missing).endsWith("missing.csv (NoSuchFileException)"));
        assertEquals("Запис 3: ціна та кількість мають бути числами", Main.describe(invalid));
    }

    @Test
    void parsesStorageOptions() {
        CliOptions options = CliOptions.parse(new String[]{"--export", "a.csv", "--import", "b.csv"});

        assertEquals(Optional.of(Path.of("a.csv")), options.exportPath());
        assertEquals(Optional.of(Path.of("b.csv")), options.importPath());
        assertTrue(CliOptions.parse(new String[0]).exportPath().isEmpty());
        assertTrue(CliOptions.parse(new String[0]).importPath().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[]{"--export"}));
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[]{"--import"}));
    }
}
