package com.bigun.kzp;

/**
 * Parses and validates a semicolon-delimited inventory CSV line.
 */
public final class CsvParser {
    /**
     * Parses one line containing name, SKU, unit price, stock, and supplier.
     *
     * @param line raw input line
     * @return valid, invalid, or skipped result
     */
    public ParseResult parse(String line) {
        if (line == null || line.isBlank()) {
            return ParseResult.skipped();
        }

        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            return ParseResult.invalid("очікується 5 полів, отримано " + fields.length);
        }

        String name = fields[0].trim();
        String sku = fields[1].trim();
        String supplier = fields[4].trim();
        if (name.isEmpty() || sku.isEmpty() || supplier.isEmpty()) {
            return ParseResult.invalid("назва, артикул і постачальник не можуть бути порожніми");
        }

        final double unitPrice;
        final int stock;
        try {
            unitPrice = Double.parseDouble(fields[2].trim());
            stock = Integer.parseInt(fields[3].trim());
        } catch (NumberFormatException exception) {
            return ParseResult.invalid("ціна та кількість мають бути числами");
        }

        if (!Double.isFinite(unitPrice)) {
            return ParseResult.invalid("ціна має бути скінченним числом");
        }
        if (unitPrice < 0 || stock < 0) {
            return ParseResult.invalid("ціна та кількість не можуть бути від'ємними");
        }
        return ParseResult.valid(new InventoryItem(name, sku, unitPrice, stock, supplier));
    }
}
