package com.bigun.kzp;

/**
 * Parses and validates a semicolon-delimited inventory CSV line.
 */
public final class CsvParser {
    /**
     * Parses one line in the {@code sku;name;stock;unitPrice;supplier} format.
     *
     * @param line raw input line
     * @return valid result, or invalid result with a reason (a blank line is invalid too)
     */
    public ParseResult parse(String line) {
        if (line == null || line.isBlank()) {
            return ParseResult.invalid("порожній рядок");
        }

        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            return ParseResult.invalid("очікується 5 полів, отримано " + fields.length);
        }

        String sku = fields[0].trim();
        String name = fields[1].trim();
        String supplier = fields[4].trim();
        if (sku.isEmpty() || name.isEmpty() || supplier.isEmpty()) {
            return ParseResult.invalid("назва, артикул і постачальник не можуть бути порожніми");
        }

        final int stock;
        final double unitPrice;
        try {
            stock = Integer.parseInt(fields[2].trim());
            unitPrice = Double.parseDouble(fields[3].trim());
        } catch (NumberFormatException exception) {
            return ParseResult.invalid("ціна та кількість мають бути числами");
        }

        if (!Double.isFinite(unitPrice)) {
            return ParseResult.invalid("ціна має бути скінченним числом");
        }
        if (unitPrice < 0 || stock < 0) {
            return ParseResult.invalid("ціна та кількість не можуть бути від'ємними");
        }
        return ParseResult.valid(new InventoryItem(sku, name, stock, unitPrice, supplier));
    }
}
