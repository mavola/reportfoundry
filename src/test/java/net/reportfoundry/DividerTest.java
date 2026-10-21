package net.reportfoundry;

import net.reportfoundry.style.Alignment;
import net.reportfoundry.style.RgbColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DividerTest {

    @Test
    void shouldGenerateDefaultDivider() {
        assertDoesNotThrow(() -> ReportFoundry.forge()
                .text("Before")
                .divider()
                .text("After")
                .toBytes()
        );
    }

    @Test
    void shouldGenerateStyledDivider() {
        assertDoesNotThrow(() -> ReportFoundry.forge()
                .text("Before")
                .divider(style -> style
                        .color(new RgbColor(100, 100, 100))
                        .thickness(2)
                        .width(50)
                        .align(Alignment.CENTER))
                .text("After")
                .toBytes()
        );
    }
}