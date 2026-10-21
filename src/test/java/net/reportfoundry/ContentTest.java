package net.reportfoundry;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentTest {

    @Test
    void shouldRenderTitleAndText() throws Exception {
        byte[] pdf = ReportFoundry.forge()
                .title("ReportFoundry")
                .text("Forge professional PDFs with Java.")
                .toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);

            assertTrue(text.contains("ReportFoundry"));
            assertTrue(text.contains("Forge professional PDFs with Java."));
        }
    }

    @Test
    void tableShouldContainExpectedValues() throws Exception {
        record Product(String name, int quantity) {
        }

        List<Product> products = List.of(
                new Product("Mechanical Keyboard", 2),
                new Product("Wireless Mouse", 5),
                new Product("Monitor", 2)
        );

        byte[] pdf = ReportFoundry.forge()
                .table(products, table -> table
                        .column("Product", Product::name, 70)
                        .column("Quantity", Product::quantity, 30)
                ).toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);

            assertTrue(text.contains("Product"));
            assertTrue(text.contains("Quantity"));
            assertTrue(text.contains("Mechanical Keyboard"));
            assertTrue(text.contains("Wireless Mouse"));
            assertTrue(text.contains("2"));
            assertTrue(text.contains("5"));
        }
    }
}