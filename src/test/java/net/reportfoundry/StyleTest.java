package net.reportfoundry;

import net.reportfoundry.style.Alignment;
import net.reportfoundry.style.Style;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class StyleTest {

    @Test
    void shouldRenderBoldText() {
        assertDoesNotThrow(() -> ReportFoundry.forge()
                .text("Bold text", Style::bold)
                .toBytes()
        );
    }

    @Test
    void shouldRenderCustomFontSize() {
        assertDoesNotThrow(() -> ReportFoundry.forge()
                .text("Large text", style -> style.fontSize(20))
                .toBytes()
        );
    }

    @Test
    void shouldRenderAllTextAlignments() {
        assertDoesNotThrow(() -> ReportFoundry.forge()
                .text("Left", style -> style.align(Alignment.LEFT))
                .text("Center", style -> style.align(Alignment.CENTER))
                .text("Right", style -> style.align(Alignment.RIGHT))
                .toBytes()
        );
    }
}