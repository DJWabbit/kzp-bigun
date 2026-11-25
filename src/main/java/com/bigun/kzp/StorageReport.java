package com.bigun.kzp;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Exports and imports parts and describes the result for the console.
 */
public final class StorageReport {
    private StorageReport() {
    }

    /**
     * Writes parts to a CSV file, reads them back, and compares both lists.
     *
     * @param path CSV file
     * @param parts parts to export
     * @return report text ending with a platform newline
     * @throws DataStorageException if the file cannot be written or read
     */
    public static String exportParts(Path path, List<SparePart> parts) throws DataStorageException {
        List<SparePartRecord> records = parts.stream()
                .map(SparePartRecord::from)
                .toList();
        CsvStorage.write(path, SparePartRecord.class, records);
        boolean restored = records.equals(readRecords(path));
        return String.format(Locale.ROOT, "Експорт: записів %d%nRound-trip: %s%n",
                records.size(), restored ? "так" : "ні");
    }

    /**
     * Reads parts from a CSV file.
     *
     * @param path CSV file
     * @return report text ending with a platform newline
     * @throws DataStorageException if the file cannot be read or its content is wrong
     */
    public static String importParts(Path path) throws DataStorageException {
        List<SparePart> parts = readRecords(path).stream()
                .map(SparePartRecord::toPart)
                .toList();
        String lines = parts.stream()
                .map(part -> String.format(Locale.ROOT, "- %s (%s)%n", part.getSku(), part.getKind().getLabel()))
                .collect(Collectors.joining());
        return String.format(Locale.ROOT, "Імпорт: записів %d%n%s", parts.size(), lines);
    }

    private static List<SparePartRecord> readRecords(Path path) throws DataStorageException {
        return CsvStorage.read(path, SparePartRecord.class, SparePartRecord::fromFields);
    }
}
