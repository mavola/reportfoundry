package net.reportfoundry;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ReportForgeTest {

    @Test
    void shouldGeneratePdfAsBytes() {
        byte[] pdf = ReportFoundry.forge()
                .title("ReportFoundry")
                .text("Generated in memory.")
                .toBytes();

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generatedBytesShouldContainPdfHeader() {
        byte[] pdf = ReportFoundry.forge()
                .title("ReportFoundry")
                .toBytes();

        assertTrue(pdf.length >= 4);

        String header = new String(pdf, 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", header);
    }
}