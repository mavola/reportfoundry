package net.reportfoundry;

import net.reportfoundry.style.RgbColor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BoxTest {

    @Test
    void shouldGenerateBox() {
        assertDoesNotThrow(() -> ReportFoundry.forge()
                .box(box -> box
                        .style(style -> style
                                .background(new RgbColor(245, 247, 250))
                                .borderColor(new RgbColor(180, 180, 180))
                                .padding(15)
                        )
                        .text("Payment information")
                        .text("Bank: Example Bank")
                        .text("Account: 000-123456")
                ).toBytes()
        );
    }

    @Test
    void boxShouldMoveToNextPageWhenItDoesNotFit() throws Exception {
        ReportForge report = ReportFoundry.forge()
                .margins(60)
                .title("Box pagination");

        for (int i = 0; i < 24; i++) {
            report.text("Content before the box " + i);
        }

        report.box(box -> box
                .style(style -> style.padding(20))
                .text("This box should remain together.")
                .text("Second line.")
                .text("Third line.")
        );

        byte[] pdf = report.toBytes();
        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(2, document.getNumberOfPages());
        }
    }
}
