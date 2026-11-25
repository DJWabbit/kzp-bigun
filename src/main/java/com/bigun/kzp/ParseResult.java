package com.bigun.kzp;

import java.util.Optional;

/**
 * Result of validating a single CSV line.
 *
 * @param item parsed item, or empty when the line is invalid
 * @param error validation message, or empty when the line is valid
 */
public record ParseResult(Optional<InventoryItem> item, Optional<String> error) {
    /**
     * Creates a result for a valid inventory item.
     *
     * @param item validated item
     * @return successful result
     */
    public static ParseResult valid(InventoryItem item) {
        return new ParseResult(Optional.of(item), Optional.empty());
    }

    /**
     * Creates a result for a rejected CSV line.
     *
     * @param reason explanation of the validation failure
     * @return failed result
     */
    public static ParseResult invalid(String reason) {
        return new ParseResult(Optional.empty(), Optional.of(reason));
    }
}
