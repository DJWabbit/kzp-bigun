package com.bigun.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Writes and reads CSV files in UTF-8.
 */
public final class CsvStorage {
    /**
     * Restores an object from CSV fields.
     *
     * @param <T> record type
     */
    @FunctionalInterface
    public interface RecordFactory<T> {
        /**
         * Creates an object.
         *
         * @param fields values in the export order
         * @return restored object
         * @throws DataStorageException if the values are wrong
         */
        T create(List<String> fields) throws DataStorageException;
    }

    private CsvStorage() {
    }

    /**
     * Writes records to a file; previous content is replaced.
     *
     * @param path CSV file
     * @param type class of the records
     * @param records records to write
     * @param <T> record type
     * @throws DataStorageException if the records cannot be exported or the file cannot be written
     */
    public static <T> void write(Path path, Class<T> type, List<? extends T> records)
            throws DataStorageException {
        String csv = CsvExporter.toCsv(type, records);
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, csv, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося записати CSV " + path, exception);
        }
    }

    /**
     * Reads records from a file that was written by {@link #write(Path, Class, List)}.
     *
     * @param path CSV file
     * @param type class of the records
     * @param factory restores an object from its fields
     * @param <T> record type
     * @return unmodifiable list of records in the file order
     * @throws DataStorageException if the file cannot be read or its content is wrong
     */
    public static <T> List<T> read(Path path, Class<T> type, RecordFactory<? extends T> factory)
            throws DataStorageException {
        List<List<String>> rows;
        try {
            rows = CsvDocumentParser.parse(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося прочитати CSV " + path, exception);
        }
        List<String> header = CsvExporter.header(type);
        if (rows.isEmpty() || !rows.get(0).equals(header)) {
            throw new DataStorageException("Очікується заголовок CSV: " + String.join(",", header));
        }
        Repository<T> records = new Repository<>();
        for (int index = 1; index < rows.size(); index++) {
            try {
                records.add(factory.create(rows.get(index)));
            } catch (DataStorageException exception) {
                // Нумерація разом із заголовком збігається з нумерацією парсера.
                throw new DataStorageException("Запис " + (index + 1) + ": " + exception.getMessage(), exception);
            }
        }
        return records.all();
    }
}
