package com.bigun.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public final class InventoryFileReader {
    private final CsvParser parser;

    public InventoryFileReader() {
        this(new CsvParser());
    }

    InventoryFileReader(CsvParser parser) {
        this.parser = parser;
    }

    
    public ReadResult read(Path path) throws IOException {
        List<InventoryItem> items = new ArrayList<>();
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

    
    public record ReadResult(List<InventoryItem> items, List<String> errors) {
       
        public ReadResult {
            items = List.copyOf(items);
            errors = List.copyOf(errors);
        }
    }
}
