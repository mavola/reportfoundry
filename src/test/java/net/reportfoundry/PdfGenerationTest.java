package net.reportfoundry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfGenerationTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldExportPdfToOutputStream() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ReportFoundry.forge()
                .title("OutputStream test")
                .text("ReportFoundry")
                .export(outputStream);

        assertTrue(outputStream.size() > 0);
    }

    @Test
    void shouldExportPdfToFile() throws Exception {
        Path output = tempDirectory.resolve("report.pdf");

        ReportFoundry.forge()
                .title("File test")
                .text("ReportFoundry")
                .export(output);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }
}