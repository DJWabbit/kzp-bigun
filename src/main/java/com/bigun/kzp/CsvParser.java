package com.bigun.kzp;

import java.util.Objects;

/**
 * Converts inventory CSV lines into parse results without interrupting the reading loop.
 *
 * <p>The record structure and its rules belong to {@link SparePart#fromCsv(String)}.
 */
public final class CsvParser {
    /**
     * Parses one line in the {@code sku;name;stock;unitPrice;supplier} format.
     *
     * @param line raw input line; null is treated as a blank line
     * @return valid result, or invalid result with a reason (a blank line is invalid too)
     */
    public ParseResult parse(String line) {
        try {
            return ParseResult.valid(SparePart.fromCsv(Objects.requireNonNullElse(line, "")));
        } catch (IllegalArgumentException exception) {
            return ParseResult.invalid(exception.getMessage());
        }
    }
}
