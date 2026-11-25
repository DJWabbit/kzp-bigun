package com.bigun.kzp;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Потокові запити Stream API для аналізу складу автозапчастин (варіант №4).
 */
public final class PartStreamQueries {

    private PartStreamQueries() {
    }

    /**
     * 1. Відбір деталей із залишком нижче заданого порогу (filter).
     *
     * @param parts деталі складу
     * @param threshold поріг залишку; деталь із залишком на межі не входить до результату
     * @return незмінний список у порядку вхідних даних
     */
    public static List<SparePart> findLowStock(List<SparePart> parts, int threshold) {
        Objects.requireNonNull(parts, "Список деталей не може бути null");
        return parts.stream()
                .filter(part -> part.getStock() < threshold)
                .toList();
    }

    /**
     * 2. Отримання списку артикулів усіх деталей (map).
     *
     * @param parts деталі складу
     * @return незмінний список артикулів у порядку вхідних даних, разом із дублікатами
     */
    public static List<String> extractSkus(List<SparePart> parts) {
        Objects.requireNonNull(parts, "Список деталей не може бути null");
        return parts.stream()
                .map(SparePart::getSku)
                .toList();
    }

    /**
     * 3. Групування: сумарна вартість запасу за кожним виробником (groupingBy).
     *
     * @param parts деталі складу
     * @return карта «виробник → вартість запасу», впорядкована за виробником
     */
    public static Map<String, Double> stockValueBySupplier(List<SparePart> parts) {
        Objects.requireNonNull(parts, "Список деталей не може бути null");
        return parts.stream()
                .collect(Collectors.groupingBy(
                        SparePart::getSupplier,
                        TreeMap::new,
                        Collectors.summingDouble(part -> part.getUnitPrice() * part.getStock())
                ));
    }

    /**
     * 4. Зведена статистика цін деталей (summarizingDouble).
     *
     * @param parts деталі складу
     * @return статистика; для порожнього списку кількість дорівнює нулю, а мінімум і максимум нескінченні
     */
    public static DoubleSummaryStatistics priceStatistics(List<SparePart> parts) {
        Objects.requireNonNull(parts, "Список деталей не може бути null");
        return parts.stream()
                .collect(Collectors.summarizingDouble(SparePart::getUnitPrice));
    }

    /**
     * 5. Топ-N найдорожчих деталей: ціна спаданням, потім назва зростанням.
     *
     * @param parts деталі складу
     * @param limit найбільша кількість деталей у результаті
     * @return незмінний список найдорожчих деталей
     * @throws IllegalArgumentException якщо ліміт від'ємний
     */
    public static List<SparePart> topExpensiveParts(List<SparePart> parts, int limit) {
        Objects.requireNonNull(parts, "Список деталей не може бути null");
        if (limit < 0) {
            throw new IllegalArgumentException("Ліміт не може бути від'ємним");
        }
        Comparator<SparePart> comparator = Comparator
                .comparingDouble(SparePart::getUnitPrice)
                .reversed()
                .thenComparing(SparePart::getName);
        return parts.stream()
                .sorted(comparator)
                .limit(limit)
                .toList();
    }

    /**
     * 6. Пошук деталі за артикулом із поверненням Optional.
     *
     * <p>Регістр враховується, бо він впливає на вид деталі.
     *
     * @param parts деталі складу
     * @param sku артикул; пробіли навколо нього відкидаються
     * @return перша деталь із таким артикулом або порожній результат
     */
    public static Optional<SparePart> findBySku(List<SparePart> parts, String sku) {
        Objects.requireNonNull(parts, "Список деталей не може бути null");
        if (sku == null || sku.isBlank()) {
            return Optional.empty();
        }
        String normalized = sku.trim();
        return parts.stream()
                .filter(part -> part.getSku().equals(normalized))
                .findFirst();
    }
}
