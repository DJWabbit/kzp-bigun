package com.bigun.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a UTF-8 inventory CSV file and validates its lines.
 */
public final class InventoryFileReader {
    private final CsvParser parser;

    /**
     * Creates a file reader using the standard CSV parser.
     */
    public InventoryFileReader() {
        this(new CsvParser());
    }

    /**
     * Creates a file reader with the given parser.
     *
     * @param parser parser applied to every line
     */
    InventoryFileReader(CsvParser parser) {
        this.parser = parser;
    }

    /**
     * Reads and validates all records while retaining source line numbers.
     *
     * @param path CSV file path
     * @return parsed inventory and validation errors
     * @throws IOException if the file cannot be read
     */
    public ReadResult read(Path path) throws IOException {
        List<SparePart> items = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            ParseResult result = parser.parse(lines.get(index));
            result.item().ifPresent(items::add);
            result.error().ifPresent(error -> errors.add("Рядок " + lineNumber + ": " + error));
        }
        return new ReadResult(items, errors);
    }

    /**
     * Parsed items and validation errors found in the input file.
     *
     * @param items valid spare parts
     * @param errors line-numbered validation errors
     */
    public record ReadResult(List<SparePart> items, List<String> errors) {
        /**
         * Copies the supplied lists to keep the result immutable.
         *
         * @param items valid spare parts
         * @param errors line-numbered validation errors
         */
        public ReadResult {
            items = List.copyOf(items);
            errors = List.copyOf(errors);
        }
    }
}
