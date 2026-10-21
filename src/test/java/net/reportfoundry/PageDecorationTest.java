package net.reportfoundry;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PageDecorationTest {

    @Test
    void shouldRenderHeaderAndFooter() throws Exception {
        byte[] pdf = ReportFoundry.forge()
                .header("ReportFoundry Header")
                .footer("ReportFoundry Footer")
                .title("Document")
                .text("Document content")
                .toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);

            assertTrue(text.contains("ReportFoundry Header"));
            assertTrue(text.contains("ReportFoundry Footer"));
        }
    }

    @Test
    void shouldRenderPageNumbers() throws Exception {
        byte[] pdf = ReportFoundry.forge()
                .title("First page")
                .pageBreak()
                .title("Second page")
                .pageNumbers()
                .toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);

            assertTrue(text.contains("Page 1 of 2"));
            assertTrue(text.contains("Page 2 of 2"));
        }
    }

    @Test
    void shouldRenderCustomPageNumberPattern() throws Exception {
        byte[] pdf = ReportFoundry.forge()
                .title("First page")
                .pageBreak()
                .title("Second page")
                .pageNumbers("{page} / {total}")
                .toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);

            assertTrue(text.contains("1 / 2"));
            assertTrue(text.contains("2 / 2"));
        }
    }
}
