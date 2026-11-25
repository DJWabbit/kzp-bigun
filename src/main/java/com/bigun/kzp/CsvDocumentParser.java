package com.bigun.kzp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Parses a whole CSV document; line breaks inside quotes belong to the field.
 */
public final class CsvDocumentParser {
    private enum State {
        /** Beginning of a field. */
        START,
        /** Inside a field without quotes. */
        PLAIN,
        /** Inside quotes. */
        QUOTED,
        /** After a quote inside a quoted field: an escaped quote or the end of the field. */
        QUOTE
    }

    private final String text;
    private final List<List<String>> records = new ArrayList<>();
    private final StringBuilder field = new StringBuilder();
    private List<String> fields = new ArrayList<>();
    private State state = State.START;

    private CsvDocumentParser(String text) {
        this.text = text;
    }

    /**
     * Splits a document into records and fields.
     *
     * @param text CSV document with line breaks {@code \n} or {@code \r\n}
     * @return unmodifiable list of records, including the header
     * @throws DataStorageException if quotes are used incorrectly
     */
    public static List<List<String>> parse(String text) throws DataStorageException {
        Objects.requireNonNull(text, "Текст CSV не може бути null");
        return new CsvDocumentParser(text).parse();
    }

    private List<List<String>> parse() throws DataStorageException {
        for (int index = 0; index < text.length(); index++) {
            char symbol = text.charAt(index);
            if (state == State.QUOTED) {
                readQuoted(symbol);
            } else if (symbol == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                // Пара \r\n завершує запис на символі \n.
                continue;
            } else {
                readOutsideQuotes(symbol);
            }
        }
        if (state == State.QUOTED) {
            throw error("лапки не закрито");
        }
        if (state != State.START || !fields.isEmpty()) {
            endRecord();
        }
        return List.copyOf(records);
    }

    private void readQuoted(char symbol) {
        if (symbol == '"') {
            state = State.QUOTE;
        } else {
            field.append(symbol);
        }
    }

    private void readOutsideQuotes(char symbol) throws DataStorageException {
        if (symbol == ',') {
            endField();
        } else if (symbol == '\n') {
            endRecord();
        } else if (symbol == '"') {
            readQuote();
        } else if (state == State.QUOTE) {
            throw error("зайві символи після лапок");
        } else {
            field.append(symbol);
            state = State.PLAIN;
        }
    }

    private void readQuote() throws DataStorageException {
        if (state == State.PLAIN) {
            throw error("лапки всередині поля без лапок");
        }
        if (state == State.QUOTE) {
            field.append('"');
        }
        state = State.QUOTED;
    }

    private void endField() {
        fields.add(field.toString());
        field.setLength(0);
        state = State.START;
    }

    private void endRecord() {
        endField();
        records.add(List.copyOf(fields));
        fields = new ArrayList<>();
    }

    private DataStorageException error(String reason) {
        return new DataStorageException("Запис " + (records.size() + 1) + ": " + reason);
    }
}
