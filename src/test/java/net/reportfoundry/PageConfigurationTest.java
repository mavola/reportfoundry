package net.reportfoundry;

import net.reportfoundry.document.PageSize;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageConfigurationTest {

    private static final float DELTA = 0.1f;

    @Test
    void shouldGenerateA4Page() throws Exception {
        byte[] pdf = ReportFoundry.forge()
                .page(PageSize.A4)
                .title("A4")
                .toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            float width = document.getPage(0).getMediaBox().getWidth();
            float height = document.getPage(0).getMediaBox().getHeight();

            assertEquals(PageSize.A4.getWidth(), width, DELTA);
            assertEquals(PageSize.A4.getHeight(), height, DELTA);
        }
    }

    @Test
    void customMarginsShouldStillGenerateDocument() throws Exception {
        byte[] pdf = ReportFoundry.forge()
                .page(PageSize.A4)
                .margins(30, 40, 50, 60)
                .title("Custom margins")
                .text("Content")
                .toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(1, document.getNumberOfPages());
        }
    }
}
