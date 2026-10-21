package net.reportfoundry;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.exception.LayoutException;
import net.reportfoundry.exception.RenderException;
import net.reportfoundry.exception.ReportFoundryException;
import net.reportfoundry.style.RgbColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ExceptionTest {

    @TempDir
    Path tempDirectory;

    @Test
    void invalidConfigurationShouldThrowConfigurationException() {
        assertThrows(ConfigurationException.class, () -> new RgbColor(256, 0, 0));
    }

    @Test
    void invalidTableConfigurationShouldThrowConfigurationException() {
        record Product(String name) {
        }

        assertThrows(ConfigurationException.class, () -> ReportFoundry.forge()
                .table(List.of(new Product("Keyboard")), table -> table
                        .column("A", Product::name, 60)
                        .column("B", Product::name, 60)
                ).toBytes()
        );
    }

    @Test
    void impossibleLayoutShouldThrowLayoutException() {
        assertThrows(LayoutException.class, () -> ReportFoundry.forge()
                .image(tempDirectory.resolve("unused.png"), 10000, 10, net.reportfoundry.style.Alignment.LEFT)
                .toBytes());
    }

    @Test
    void outputFailureShouldThrowRenderException() {
        Path invalidPath = tempDirectory.resolve("missing-directory").resolve("report.pdf");

        assertThrows(RenderException.class, () -> ReportFoundry.forge()
                .title("Render failure")
                .export(invalidPath));
    }

    @Test
    void specificExceptionsShouldExtendReportFoundryException() {
        assertThrows(ReportFoundryException.class, () -> new RgbColor(-1, 0, 0));
    }
}