package com.bigun.kzp;

import java.nio.file.Path;

/**
 * Parsed command-line options for the inventory report application.
 *
 * @param input input CSV path
 * @param output report output path
 * @param help whether help was requested
 * @param version whether version was requested
 */
public record CliOptions(Path input, Path output, boolean help, boolean version) {
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
        for (int index = 0; index < args.length; index++) {
            switch (args[index]) {
                case "--help" -> help = true;
                case "--version" -> version = true;
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
                default -> throw new IllegalArgumentException("Невідомий аргумент: " + args[index]);
            }
        }
        return new CliOptions(input, output, help, version);
    }
}
