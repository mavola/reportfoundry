package net.reportfoundry;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.exception.TemplateException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TemplateFilesystemTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGeneratePdfFromFilesystemTemplate() throws IOException {
        Path template = tempDir.resolve("invoice.html");

        String html = """
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org">
                <head>
                    <meta charset="UTF-8"/>
                </head>
                <body>
                    <h1 th:text="${title}">Title</h1>
                    <p th:text="${customer}">Customer</p>
                </body>
                </html>
                """;

        Files.writeString(template, html, StandardCharsets.UTF_8);

        byte[] pdf = ReportFoundry.template(template)
                .data("title", "Filesystem Report")
                .data("customer", "ACME Corporation")
                .toBytes();

        assertTrue(pdf.length > 0);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);

            assertTrue(text.contains("Filesystem Report"));
            assertTrue(text.contains("ACME Corporation"));
        }
    }

    @Test
    void missingFilesystemTemplateShouldThrowTemplateException() {
        Path template = tempDir.resolve("missing.html");

        assertThrows(TemplateException.class, () -> ReportFoundry.template(template).toBytes());
    }

    @Test
    void nullFilesystemTemplateShouldThrowConfigurationException() {
        assertThrows(ConfigurationException.class, () -> ReportFoundry.template((Path) null));
    }

    @Test
    void shouldExportFilesystemTemplateToFile() throws IOException {
        Path template = tempDir.resolve("report.html");
        Path output = tempDir.resolve("report.pdf");

        Files.writeString(template, "<html><body><p>Filesystem Export</p></body></html>");

        ReportFoundry.template(template).export(output);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }
}