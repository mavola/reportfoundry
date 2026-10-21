package net.reportfoundry;

import net.reportfoundry.style.Alignment;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableTest {

    record Product(String name, int quantity, String price) {
    }

    @Test
    void shouldGenerateTable() {
        List<Product> products = List.of(
                new Product("Mechanical Keyboard", 2, "$ 125.50"),
                new Product("Wireless Mouse", 5, "$ 45.90"),
                new Product("Monitor", 2, "$ 399.99")
        );

        assertDoesNotThrow(() -> ReportFoundry.forge()
                .table(products, table -> table
                        .column("Product", Product::name, 55, Alignment.LEFT)
                        .column("Quantity", Product::quantity, 15, Alignment.CENTER)
                        .column("Price", Product::price, 30, Alignment.RIGHT)
                ).toBytes()
        );
    }

    @Test
    void longTableShouldCreateMultiplePages() throws Exception {
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            products.add(new Product("Product " + i, i + 1, "$ 100.00"));
        }

        byte[] pdf = ReportFoundry.forge()
                .table(products, table -> table
                        .column("Product", Product::name, 55, Alignment.LEFT)
                        .column("Quantity", Product::quantity, 15, Alignment.CENTER)
                        .column("Price", Product::price, 30, Alignment.RIGHT)
                ).toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertTrue(document.getNumberOfPages() > 1);
        }
    }

    @Test
    void tableShouldSupportWrappedCellContent() {
        List<Product> products = List.of(
                new Product("Mechanical Keyboard with RGB illumination and programmable keys", 2, "$ 125.50"),
                new Product("USB-C Docking Station with Ethernet HDMI and multiple USB ports", 1, "$ 189.00")
        );

        assertDoesNotThrow(() -> ReportFoundry.forge()
                .table(products, table -> table
                        .column("Product", Product::name, 55, Alignment.LEFT)
                        .column("Quantity", Product::quantity, 15, Alignment.CENTER)
                        .column("Price", Product::price, 30, Alignment.RIGHT)
                ).toBytes()
        );
    }

    @Test
    void tableHeaderShouldRepeatOnEveryPage() throws Exception {
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            products.add(new Product("Product " + i, i + 1, "$ 100.00"));
        }

        byte[] pdf = ReportFoundry.forge()
                .table(products, table -> table
                        .column("Product", Product::name, 55, Alignment.LEFT)
                        .column("Quantity", Product::quantity, 15, Alignment.CENTER)
                        .column("Price", Product::price, 30, Alignment.RIGHT))
                .toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();

            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String pageText = stripper.getText(document);

                assertTrue(pageText.contains("Product"));
                assertTrue(pageText.contains("Quantity"));
                assertTrue(pageText.contains("Price"));
            }
        }
    }
}
