# Кросплатформні засоби програмування

* **Студент:** Бігун Ілля
* **Пошта:** Illia.Bihun.Kl.2024@lpnu.ua
* **Рівень:** 2
* **Варіант №4:** Склад автозапчастин
* **Викладач:** Максим Олексіїв


# Лабораторна робота №2 — склад автозапчастин

Лабораторна робота №2 (рівень 2, варіант №4) рефакторить консольний застосунок обліку складу автозапчастин. Предметна модель `SparePart` інкапсулює дані та правила коректності деталі, а `StockValue` представляє перевірені підсумкові показники складу. Програма читає CSV, обчислює статистику та виводить звіт у консоль і файл.

## Формат вхідного CSV

Файл кодується UTF-8. Кожен непорожній рядок має п'ять полів, розділених крапкою з комою:

```text
name;sku;unitPrice;stock;supplier
```

`unitPrice` — невід'ємне скінченне число з десятковою крапкою; `stock` — невід'ємне ціле число. Текстові поля не можуть бути порожніми. Порожні рядки ігноруються. Приклад даних: [data/input.csv](data/input.csv).

## Збірка та запуск

Потрібен JDK 21. Maven Wrapper автоматично завантажує Maven під час першого запуску.

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd package
java -jar target/kzp-bigun-1.0-SNAPSHOT.jar
java -jar target/kzp-bigun-1.0-SNAPSHOT.jar --input data/input.csv --output out/report.txt
java -jar target/kzp-bigun-1.0-SNAPSHOT.jar --version
java -jar target/kzp-bigun-1.0-SNAPSHOT.jar --help
```

На Linux/macOS для збірки використовуйте `./mvnw clean verify` та `./mvnw package`.
Без параметрів зчитується `data/input.csv`, а звіт записується в `out/report.txt` і одночасно виводиться в консоль. Метадані версії зберігаються у `src/main/resources/version.properties`.

## Предметна модель і перевірка

`SparePart` — незмінний клас-сутність із полями `private final`, геттерами та перевірками в конструкторі: текстові значення обов'язкові й непорожні, ціна скінченна та невід'ємна, кількість невід'ємна. Фабричний метод `SparePart.fromCsv(String)` створює деталь із рядка формату `name;sku;unitPrice;stock;supplier`. `toString()` форматує ціну з `Locale.ROOT`.

`StockValue` — record для сумарної кількості одиниць і вартості запасу; компактний конструктор відхиляє від'ємні значення та нечислову/нескінченну сумарну вартість.

Перевірка тестів, збірки та SpotBugs:

```powershell
.\mvnw.cmd clean verify
```

Для окремого пакування JAR:

```powershell
.\mvnw.cmd package
```
