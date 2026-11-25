package com.bigun.kzp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvRoundTripTest {
    private static final String HEADER = "артикул,виробник,назва,ціна,кількість\n";

    private static final List<SparePartRecord> STOCK = List.of(
            new SparePartRecord("BRK-001", "Гальмівні колодки", 12, 1250.5, "АвтоПостач"),
            new SparePartRecord("FLT-014-R", "Фільтр, масляний", 25, 320.0, "Мотор-Сервіс"),
            new SparePartRecord("SPK-210", "Свічка \"Іридій\"", 40, 185.75, "Деталь+"),
            new SparePartRecord("TIM-008", "Ремінь ГРМ\nкомплект", 7, 2100.0, "АвтоПостач"),
            new SparePartRecord("OIL-101-R", "Олива моторна", 10, 780.0, "Мотор-Сервіс, Львів"),
            new SparePartRecord("AMT-330", "Амортизатор", 4, 2100.0, "Деталь+"));

    @TempDir
    Path tempDir;

    @Test
    void exportMatchesSampleFileByteForByte() throws Exception {
        Path output = tempDir.resolve("stock.csv");
        CsvStorage.write(output, SparePartRecord.class, STOCK);

        assertEquals(Files.readString(Path.of("data", "stock.csv"), StandardCharsets.UTF_8).replace("\r\n", "\n"),
                Files.readString(output, StandardCharsets.UTF_8));
    }

    @Test
    void sampleFileHasEightLinesAndSixRecords() throws Exception {
        Path sample = Path.of("data", "stock.csv");

        assertEquals(8, Files.readAllLines(sample, StandardCharsets.UTF_8).size());
        assertEquals(STOCK, read(sample));
    }

    @Test
    void roundTripRestoresEqualRecordsInOrder() throws DataStorageException {
        Path file = tempDir.resolve("parts.csv");
        CsvStorage.write(file, SparePartRecord.class, STOCK);

        assertEquals(STOCK, read(file));
    }

    @Test
    void roundTripKeepsExactValues() throws DataStorageException {
        List<SparePartRecord> records = List.of(
                new SparePartRecord(" A-1 ", "  назва  ", Integer.MAX_VALUE, 0.1 + 0.2, "П"),
                new SparePartRecord("A-2", "рядок\r\nрядок", 0, Double.MIN_VALUE, "а,\"б\"\n"),
                new SparePartRecord("A-3", "\"", 1, 1.0E20, ","),
                new SparePartRecord("A-4", "Їжак і Ґава 😀", 1, 0.0, "П"));
        Path file = tempDir.resolve("exact.csv");
        CsvStorage.write(file, SparePartRecord.class, records);

        assertEquals(records, read(file));
    }

    @Test
    void roundTripRestoresSubtypes() throws DataStorageException {
        SparePart original = SparePart.fromCsv("A-1;Деталь;2;10.5;П");
        SparePart replacement = SparePart.fromCsv("A-1-R;Деталь;3;99.9;П");
        Path file = tempDir.resolve("kinds.csv");
        CsvStorage.write(file, SparePartRecord.class,
                List.of(SparePartRecord.from(original), SparePartRecord.from(replacement)));
        List<SparePart> restored = read(file).stream().map(SparePartRecord::toPart).toList();

        assertInstanceOf(OriginalPart.class, restored.get(0));
        assertInstanceOf(ReplacementPart.class, restored.get(1));
        assertEquals(List.of(original, replacement), restored);
        assertEquals(original.warrantyCost(), restored.get(0).warrantyCost());
        assertEquals(replacement.warrantyCost(), restored.get(1).warrantyCost());
    }

    @Test
    void recordEqualityCoversEveryField() {
        SparePartRecord record = new SparePartRecord("A-1", "Деталь", 1, 1.0, "П");

        assertEquals(record, new SparePartRecord("A-1", "Деталь", 1, 1.0, "П"));
        assertEquals(record.hashCode(), new SparePartRecord("A-1", "Деталь", 1, 1.0, "П").hashCode());
        assertNotEquals(record, new SparePartRecord("A-2", "Деталь", 1, 1.0, "П"));
        assertNotEquals(record, new SparePartRecord("A-1", "Інша", 1, 1.0, "П"));
        assertNotEquals(record, new SparePartRecord("A-1", "Деталь", 2, 1.0, "П"));
        assertNotEquals(record, new SparePartRecord("A-1", "Деталь", 1, 2.0, "П"));
        assertNotEquals(record, new SparePartRecord("A-1", "Деталь", 1, 1.0, "Р"));
    }

    @Test
    void recordFollowsSparePartRules() {
        assertThrows(NullPointerException.class, () -> new SparePartRecord(null, "Д", 1, 1.0, "П"));
        assertThrows(NullPointerException.class, () -> new SparePartRecord("A", null, 1, 1.0, "П"));
        assertThrows(IllegalArgumentException.class, () -> new SparePartRecord(" ", "Д", 1, 1.0, "П"));
        assertThrows(IllegalArgumentException.class, () -> new SparePartRecord("A", "Д", -1, 1.0, "П"));
        assertThrows(IllegalArgumentException.class, () -> new SparePartRecord("A", "Д", 1, Double.NaN, "П"));
        assertThrows(IllegalArgumentException.class, () -> new SparePartRecord("A", "Д", 1, 1.0, ""));
    }

    @Test
    void emptyListIsExportedAsHeaderAndReadBack() throws Exception {
        Path file = tempDir.resolve("empty.csv");
        CsvStorage.write(file, SparePartRecord.class, List.of());

        assertEquals(HEADER, Files.readString(file, StandardCharsets.UTF_8));
        assertTrue(read(file).isEmpty());
        assertTrue(read(Path.of("data", "stock-empty.csv")).isEmpty());
    }

    @Test
    void shorterExportReplacesPreviousContent() throws Exception {
        Path file = tempDir.resolve("nested").resolve("parts.csv");
        CsvStorage.write(file, SparePartRecord.class, STOCK);
        CsvStorage.write(file, SparePartRecord.class, STOCK.subList(0, 1));

        assertEquals(STOCK.subList(0, 1), read(file));
    }

    @Test
    void reportsWrongNumberWithRecordNumberAndCause() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> read(Path.of("data", "stock-invalid.csv")));

        assertEquals("Запис 3: ціна та кількість мають бути числами", exception.getMessage());
        assertInstanceOf(DataStorageException.class, exception.getCause());
        assertInstanceOf(NumberFormatException.class, exception.getCause().getCause());
    }

    @Test
    void reportsWrongFieldCount() throws IOException {
        assertFailure("Запис 2: очікується 5 полів, отримано 4", HEADER + "A,П,Д,1\n");
        assertFailure("Запис 2: очікується 5 полів, отримано 6", HEADER + "A,П,Д,1,1,\n");
        assertFailure("Запис 2: очікується 5 полів, отримано 1", HEADER + "\n");
    }

    @Test
    void reportsWrongValuesOfModel() throws IOException {
        DataStorageException exception =
                assertFailure("Запис 2: ціна та кількість не можуть бути від'ємними", HEADER + "A,П,Д,-1,1\n");

        assertInstanceOf(IllegalArgumentException.class, exception.getCause().getCause());
        assertFailure("Запис 2: назва, артикул і постачальник не можуть бути порожніми", HEADER + ",П,Д,1,1\n");
        assertFailure("Запис 2: ціна має бути скінченним числом", HEADER + "A,П,Д,NaN,1\n");
        assertFailure("Запис 2: ціна та кількість мають бути числами", HEADER + "A,П,Д,1,1.5\n");
    }

    @Test
    void reportsWrongOrMissingHeader() throws IOException {
        String expected = "Очікується заголовок CSV: артикул,виробник,назва,ціна,кількість";

        assertNull(assertFailure(expected, "").getCause());
        assertFailure(expected, "назва,артикул\n");
        assertFailure(expected, "BRK-001,АвтоПостач,Гальмівні колодки,1250.5,12\n");
    }

    @Test
    void reportsBrokenQuotes() throws IOException {
        assertFailure("Запис 2: лапки не закрито", HEADER + "A,П,\"Д,1,1\n");
    }

    @Test
    void reportsFileErrorsWithCause() throws IOException {
        DataStorageException missing = assertThrows(DataStorageException.class,
                () -> read(tempDir.resolve("missing.csv")));
        assertInstanceOf(NoSuchFileException.class, missing.getCause());

        Path broken = tempDir.resolve("broken.csv");
        Files.write(broken, new byte[]{(byte) 0xC3, (byte) 0x28});
        DataStorageException encoding = assertThrows(DataStorageException.class, () -> read(broken));
        assertInstanceOf(MalformedInputException.class, encoding.getCause());

        DataStorageException write = assertThrows(DataStorageException.class,
                () -> CsvStorage.write(tempDir, SparePartRecord.class, STOCK));
        assertInstanceOf(IOException.class, write.getCause());
    }

    private DataStorageException assertFailure(String message, String csv) throws IOException {
        Path file = tempDir.resolve("failure.csv");
        Files.writeString(file, csv, StandardCharsets.UTF_8);
        DataStorageException exception = assertThrows(DataStorageException.class, () -> read(file));
        assertEquals(message, exception.getMessage());
        return exception;
    }

    private static List<SparePartRecord> read(Path path) throws DataStorageException {
        return CsvStorage.read(path, SparePartRecord.class, SparePartRecord::fromFields);
    }
}
