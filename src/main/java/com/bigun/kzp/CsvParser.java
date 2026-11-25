package com.bigun.kzp;

public final class CsvParser {
    public ParseResult parse(String line) {
        if (line == null || line.isBlank()) {
            return ParseResult.skipped();
        }

        try {
            return ParseResult.valid(SparePart.fromCsv(line));
        } catch (IllegalArgumentException exception) {
            return ParseResult.invalid(exception.getMessage());
        }
    }
}
