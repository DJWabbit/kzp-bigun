package com.bigun.kzp;

import java.util.Optional;

public record ParseResult(Optional<InventoryItem> item, Optional<String> error) {
    public static ParseResult valid(InventoryItem item) {
        return new ParseResult(Optional.of(item), Optional.empty());
    }

    public static ParseResult invalid(String reason) {
        return new ParseResult(Optional.empty(), Optional.of(reason));
    }

    public static ParseResult skipped() {
        return new ParseResult(Optional.empty(), Optional.empty());
    }
}
