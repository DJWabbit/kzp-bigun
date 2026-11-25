package com.bigun.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Command-line application for generating an automobile-parts inventory report.
 */
public final class Main {
    private static final String HELP = """
            Використання: java -jar kzp-bigun-1.0.0.jar [опції]
              --help             показати цю довідку
              --version          показати версію у форматі version+build
              --input <шлях>     CSV-файл (за замовчуванням data/input.csv)
              --output <шлях>    файл звіту (за замовчуванням out/report.txt)
              --warranty         вивести в консоль гарантійну вартість деталей
              --queries          вивести в консоль результати запитів до складу
              --find <артикул>   знайти деталь за артикулом і вивести її в консоль
              --export <шлях>    записати деталі у CSV і перевірити відновлення
              --import <шлях>    прочитати деталі з CSV і вивести артикули
            """;

    private Main() {
    }

    /**
     * Parses arguments and runs the requested command.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            run(args);
        } catch (DataStorageException exception) {
            printError(describe(exception));
            System.exit(1);
        } catch (IllegalArgumentException | IOException exception) {
            printError(exception.getMessage());
            System.exit(1);
        }
    }

    /**
     * Prints an error as UTF-8 so that Cyrillic text does not depend on the system charset.
     *
     * @param message error description
     */
    static void printError(String message) {
        String line = String.format(Locale.ROOT, "Помилка: %s%n", message);
        System.err.writeBytes(line.getBytes(StandardCharsets.UTF_8));
        System.err.flush();
    }

    /**
     * Executes the requested mode and writes the report to the console and to the output file.
     *
     * @param args command-line arguments
     * @throws IOException if the input, output, or version resource cannot be accessed
     * @throws DataStorageException if the CSV export or import fails
     */
    private static void run(String[] args) throws IOException, DataStorageException {
        CliOptions options = CliOptions.parse(args);
        if (options.help()) {
            System.out.write(HELP.getBytes(StandardCharsets.UTF_8));
            return;
        }
        if (options.version()) {
            System.out.println(VersionInfo.formattedVersion());
            return;
        }

        InventoryFileReader.ReadResult result = new InventoryFileReader().read(options.input());
        InventoryStats stats = InventoryStats.calculate(result.items());
        String report = new ReportFormatter().format(stats, result.errors());
        System.out.write(report.getBytes(StandardCharsets.UTF_8));
        System.out.flush();
        Path output = options.output();
        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, report, StandardCharsets.UTF_8);
        if (options.warranty()) {
            print(WarrantyReport.format(result.items()));
        }
        if (options.queries()) {
            print(QueryReport.format(result.items()));
        }
        if (options.find().isPresent()) {
            print(QueryReport.formatSearch(result.items(), options.find().get()));
        }
        if (options.exportPath().isPresent()) {
            print(StorageReport.exportParts(options.exportPath().get(), result.items()));
        }
        if (options.importPath().isPresent()) {
            print(StorageReport.importParts(options.importPath().get()));
        }
    }

    /**
     * Prints text as UTF-8 so that Cyrillic does not depend on the system charset.
     *
     * @param text text to print
     */
    private static void print(String text) {
        System.out.writeBytes(text.getBytes(StandardCharsets.UTF_8));
        System.out.flush();
    }

    /**
     * Describes a storage failure and the kind of the original file error.
     *
     * @param exception storage failure
     * @return error description
     */
    static String describe(DataStorageException exception) {
        return exception.getCause() instanceof IOException cause
                ? exception.getMessage() + " (" + cause.getClass().getSimpleName() + ")"
                : exception.getMessage();
    }
}
