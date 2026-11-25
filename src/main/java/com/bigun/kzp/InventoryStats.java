package com.bigun.kzp;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Calculates the requested inventory indicators.
 *
 * @param validRecordCount number of valid records
 * @param stockValue total quantity and total value of the stock
 * @param mostExpensive part with the highest unit price, if any
 */
public record InventoryStats(int validRecordCount, StockValue stockValue,
                             Optional<SparePart> mostExpensive) {
    /**
     * Checks the calculated indicators.
     *
     * @param validRecordCount number of valid records
     * @param stockValue total quantity and total value of the stock
     * @param mostExpensive part with the highest unit price, if any
     * @throws NullPointerException if a reference component is null
     * @throws IllegalArgumentException if the record count is negative
     */
    public InventoryStats {
        if (validRecordCount < 0) {
            throw new IllegalArgumentException("Кількість коректних записів не може бути від'ємною.");
        }
        Objects.requireNonNull(stockValue, "stockValue");
        Objects.requireNonNull(mostExpensive, "mostExpensive");
    }

    /**
     * Calculates all variant 4 indicators from valid parts.
     *
     * @param items valid spare parts
     * @return calculated indicators, with an empty most-expensive part for empty input
     */
    public static InventoryStats calculate(List<SparePart> items) {
        long stock = 0;
        double value = 0;
        for (SparePart item : items) {
            stock += item.getStock();
            value += item.getUnitPrice() * item.getStock();
        }
        Optional<SparePart> mostExpensive = items.stream()
                .max(Comparator.comparingDouble(SparePart::getUnitPrice));
        return new InventoryStats(items.size(), new StockValue(stock, value), mostExpensive);
    }
}
