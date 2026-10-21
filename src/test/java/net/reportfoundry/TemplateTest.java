package net.reportfoundry;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.exception.TemplateException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateTest {

    @Test
    void shouldGeneratePdfFromHtmlTemplate() throws IOException {
        Customer customer = new Customer("ACME Corporation", "billing@acme.com");

        Invoice invoice = new Invoice("INV-2026-001", customer);

        List<Item> items = List.of(
                new Item("Mechanical Keyboard", 2, 120.50),
                new Item("Wireless Mouse", 3, 45.99),
                new Item("USB-C Dock", 1, 189.90)
        );

        byte[] pdf = ReportFoundry.template("templates/invoice.html")
                .data("invoice", invoice)
                .data("items", items)
                .toBytes();

        assertTrue(pdf.length > 0);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);

            assertTrue(text.contains("Invoice"));
            assertTrue(text.contains("INV-2026-001"));
            assertTrue(text.contains("ACME Corporation"));
            assertTrue(text.contains("billing@acme.com"));
            assertTrue(text.contains("Mechanical Keyboard"));
            assertTrue(text.contains("Wireless Mouse"));
            assertTrue(text.contains("USB-C Dock"));
        }
    }

    @Test
    void shouldGenerateValidPdfHeader() {
        byte[] pdf = ReportFoundry.template("templates/invoice.html")
                .data("invoice", new Invoice("INV-001", new Customer("ACME", "acme@test.com")))
                .data("items", List.of())
                .toBytes();

        String header = new String(pdf, 0, 4);

        assertTrue(header.startsWith("%PDF"));
    }

    @Test
    void missingTemplateShouldThrowTemplateException() {
        assertThrows(TemplateException.class, () ->
                ReportFoundry.template("templates/does-not-exist.html").toBytes()
        );
    }

    @Test
    void blankTemplatePathShouldThrowConfigurationException() {
        assertThrows(ConfigurationException.class, () ->
                ReportFoundry.template(" ")
        );
    }

    @Test
    void blankDataNameShouldThrowConfigurationException() {
        assertThrows(ConfigurationException.class, () ->
                ReportFoundry.template("templates/invoice.html").data(" ", "value")
        );
    }

    @Test
    void shouldGenerateMultiPageHtmlReport() throws IOException {
        List<Item> items = java.util.stream.IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new Item("Product " + i, i, i * 10.50))
                .toList();

        byte[] pdf = ReportFoundry.template("templates/report.html")
                .data("company", "ReportFoundry")
                .data("title", "Sales Report")
                .data("description", "September 2026")
                .data("customer", new Customer("ACME Corporation", "billing@acme.com"))
                .data("items", items)
                .toBytes();

        java.nio.file.Files.write(java.nio.file.Path.of("target/report-test.pdf"), pdf); // TODO: SACAR

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);

            assertTrue(document.getNumberOfPages() > 1);
            assertTrue(text.contains("Product 1"));
            assertTrue(text.contains("Product 100"));
        }
    }

    @Test
    void shouldRepeatTableHeaderOnMultiplePages() throws IOException {
        List<Item> items = java.util.stream.IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new Item("Product " + i, i, i * 10.50))
                .toList();

        byte[] pdf = ReportFoundry.template("templates/report.html")
                .data("company", "ReportFoundry")
                .data("title", "Sales Report")
                .data("description", "September 2026")
                .data("customer", new Customer("ACME Corporation", "billing@acme.com"))
                .data("items", items)
                .toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertTrue(document.getNumberOfPages() > 1);

            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String pageText = stripper.getText(document);

                assertTrue(pageText.contains("Product Quantity Price"), "Table header missing on page " + page);
            }
        }
    }

    record Customer(String name, String email) {
    }

    record Invoice(String number, Customer customer) {
    }

    record Item(String name, int quantity, double price) {
    }
}