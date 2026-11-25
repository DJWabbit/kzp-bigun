package com.bigun.kzp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvDocumentParserTest {
    @Test
    void parsesPlainRecords() throws DataStorageException {
        assertEquals(List.of(List.of("а", "б"), List.of("1", "2")), CsvDocumentParser.parse("а,б\n1,2\n"));
    }

    @Test
    void lastLineBreakIsOptional() throws DataStorageException {
        assertEquals(CsvDocumentParser.parse("а,б\n1,2\n"), CsvDocumentParser.parse("а,б\n1,2"));
        assertEquals(List.of(List.of("а", "")), CsvDocumentParser.parse("а,"));
        assertEquals(List.of(List.of("")), CsvDocumentParser.parse("\"\""));
    }

    @Test
    void acceptsWindowsLineBreaks() throws DataStorageException {
        assertEquals(List.of(List.of("а", "б"), List.of("1", "2")), CsvDocumentParser.parse("а,б\r\n1,\"2\"\r\n"));
    }

    @Test
    void emptyTextHasNoRecords() throws DataStorageException {
        assertEquals(List.of(), CsvDocumentParser.parse(""));
    }

    @Test
    void blankLineIsRecordWithOneEmptyField() throws DataStorageException {
        assertEquals(List.of(List.of("а"), List.of(""), List.of("б")), CsvDocumentParser.parse("а\n\nб\n"));
    }

    @Test
    void keepsEmptyFieldsAndSpaces() throws DataStorageException {
        assertEquals(List.of(List.of("", " а ", "")), CsvDocumentParser.parse(", а ,\n"));
    }

    @Test
    void readsCommaQuoteAndLineBreakInsideQuotes() throws DataStorageException {
        assertEquals(List.of(List.of("а,б", "\"в\"", "г\nд", "е\r\nж", "")),
                CsvDocumentParser.parse("\"а,б\",\"\"\"в\"\"\",\"г\nд\",\"е\r\nж\",\"\"\n"));
    }

    @Test
    void multilineFieldDoesNotSplitRecord() throws DataStorageException {
        assertEquals(2, CsvDocumentParser.parse("назва\n\"перший\nдругий\nтретій\"\n").size());
    }

    @Test
    void reportsUnclosedQuotesWithRecordNumber() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvDocumentParser.parse("а\n\"б\nв"));

        assertEquals("Запис 2: лапки не закрито", exception.getMessage());
    }

    @Test
    void rejectsSymbolsAfterClosingQuote() {
        assertThrows(DataStorageException.class, () -> CsvDocumentParser.parse("\"а\"б\n"));
        assertThrows(DataStorageException.class, () -> CsvDocumentParser.parse("\"а\" ,б\n"));
    }

    @Test
    void rejectsQuoteInsidePlainField() {
        assertThrows(DataStorageException.class, () -> CsvDocumentParser.parse("а\"б\n"));
    }

    @Test
    void resultIsUnmodifiable() throws DataStorageException {
        List<List<String>> records = CsvDocumentParser.parse("а,б\n");

        assertThrows(UnsupportedOperationException.class, records::clear);
        assertThrows(UnsupportedOperationException.class, () -> records.get(0).set(0, "в"));
        assertThrows(NullPointerException.class, () -> CsvDocumentParser.parse(null));
    }
}
