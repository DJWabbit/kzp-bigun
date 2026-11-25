package com.bigun.kzp;

/**
 * Kind of a spare part; recognizes the kind by SKU and creates the matching subtype.
 */
public enum PartKind {
    /** Part made by the original manufacturer. */
    ORIGINAL("оригінальна") {
        @Override
        public SparePart create(String sku, String name, int stock, double unitPrice, String supplier) {
            return new OriginalPart(sku, name, stock, unitPrice, supplier);
        }
    },
    /** Replacement part; its SKU ends with {@code -R}. */
    REPLACEMENT("замінник") {
        @Override
        public SparePart create(String sku, String name, int stock, double unitPrice, String supplier) {
            return new ReplacementPart(sku, name, stock, unitPrice, supplier);
        }
    };

    private final String label;

    PartKind(String label) {
        this.label = label;
    }

    /**
     * Returns the text shown in reports.
     *
     * @return kind label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Recognizes the kind by the SKU suffix.
     *
     * @param sku stock keeping unit, may be null
     * @return {@link #REPLACEMENT} for the {@code -R} suffix, otherwise {@link #ORIGINAL}
     */
    public static PartKind fromSku(String sku) {
        if (sku != null && sku.trim().endsWith("-R")) {
            return REPLACEMENT;
        }
        return ORIGINAL;
    }

    /**
     * Creates a part of this kind.
     *
     * @param sku stock keeping unit
     * @param name part name
     * @param stock non-negative quantity in stock
     * @param unitPrice non-negative finite price per unit
     * @param supplier supplier name
     * @return created part
     * @throws NullPointerException if a text field is null
     * @throws IllegalArgumentException if a field violates its invariant
     */
    public abstract SparePart create(String sku, String name, int stock, double unitPrice, String supplier);
}
