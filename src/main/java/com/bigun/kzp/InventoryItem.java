package com.bigun.kzp;

/**
 * An automobile part stored in the inventory.
 *
 * @param name part name
 * @param sku stock keeping unit
 * @param unitPrice price of one item
 * @param stock number of items in stock
 * @param supplier supplier name
 */
public record InventoryItem(String name, String sku, double unitPrice, int stock, String supplier) {
}
