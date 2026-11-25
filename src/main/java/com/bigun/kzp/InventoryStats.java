package com.bigun.kzp;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Calculates the requested inventory indicators.
 *
 * @param validRecordCount number of valid records
 * @param totalStock sum of all stock quantities
 * @param totalValue sum of price multiplied by stock
 * @param mostExpensive part with the highest unit price, if any
 */
public record InventoryStats(int validRecordCount, long totalStock, double totalValue,
                             Optional<InventoryItem> mostExpensive) {
    /**
     * Calculates all variant 4 indicators from valid items.
     *
     * @param items valid inventory records
     * @return calculated indicators, with an empty most-expensive item for empty input
     */
    public static InventoryStats calculate(List<InventoryItem> items) {
        long stock = 0;
        double value = 0;
        for (InventoryItem item : items) {
            stock += item.stock();
            value += item.unitPrice() * item.stock();
        }
        Optional<InventoryItem> mostExpensive = items.stream()
                .max(Comparator.comparingDouble(InventoryItem::unitPrice));
        return new InventoryStats(items.size(), stock, value, mostExpensive);
    }
}
