package com.bigun.kzp;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Parsed command-line options for the inventory report application.
 *
 * @param input input CSV path
 * @param output report output path
 * @param help whether help was requested
 * @param version whether version was requested
 * @param warranty whether the warranty report was requested
 * @param queries whether the query report was requested
 * @param find SKU to search for, if requested
 * @param exportPath CSV file to export parts to, if requested
 * @param importPath CSV file to import parts from, if requested
 */
public record CliOptions(Path input, Path output, boolean help, boolean version, boolean warranty,
                         boolean queries, Optional<String> find, Optional<Path> exportPath,
                         Optional<Path> importPath) {
    /**
     * Parses command line options and their path values.
     *
     * @param args command-line arguments
     * @return parsed options
     * @throws IllegalArgumentException for unknown options or missing values
     */
    public static CliOptions parse(String[] args) {
        Path input = Path.of("data", "input.csv");
        Path output = Path.of("out", "report.txt");
        boolean help = false;
        boolean version = false;
        boolean warranty = false;
        boolean queries = false;
        Optional<String> find = Optional.empty();
        Optional<Path> exportPath = Optional.empty();
        Optional<Path> importPath = Optional.empty();
        for (int index = 0; index < args.length; index++) {
            switch (args[index]) {
                case "--help" -> help = true;
                case "--version" -> version = true;
                case "--warranty" -> warranty = true;
                case "--queries" -> queries = true;
                case "--find" -> {
                    if (++index >= args.length) {
                        throw new IllegalArgumentException("Для --find потрібно вказати артикул.");
                    }
                    find = Optional.of(args[index]);
                }
                case "--input" -> {
                    if (++index >= args.length) {
                        throw new IllegalArgumentException("Для --input потрібно вказати шлях.");
                    }
                    input = Path.of(args[index]);
                }
                case "--output" -> {
                    if (++index >= args.length) {
                        throw new IllegalArgumentException("Для --output потрібно вказати шлях.");
                    }
                    output = Path.of(args[index]);
                }
                case "--export" -> {
                    if (++index >= args.length) {
                        throw new IllegalArgumentException("Для --export потрібно вказати шлях.");
                    }
                    exportPath = Optional.of(Path.of(args[index]));
                }
                case "--import" -> {
                    if (++index >= args.length) {
                        throw new IllegalArgumentException("Для --import потрібно вказати шлях.");
                    }
                    importPath = Optional.of(Path.of(args[index]));
                }
                default -> throw new IllegalArgumentException("Невідомий аргумент: " + args[index]);
            }
        }
        return new CliOptions(input, output, help, version, warranty, queries, find, exportPath, importPath);
    }
}
