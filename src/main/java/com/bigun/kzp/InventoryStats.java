package com.bigun.kzp;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record InventoryStats(int validRecordCount, long totalStock, double totalValue,
                             Optional<InventoryItem> mostExpensive) {
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
