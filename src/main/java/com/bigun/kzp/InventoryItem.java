package com.bigun.kzp;

/**
 * An automobile part stored in the inventory.
 *
 * <p>The components follow the field order of the variant 4 CSV record.
 *
 * @param sku stock keeping unit
 * @param name part name
 * @param stock number of items in stock
 * @param unitPrice price of one item
 * @param supplier supplier name
 */
public record InventoryItem(String sku, String name, int stock, double unitPrice, String supplier) {
}
