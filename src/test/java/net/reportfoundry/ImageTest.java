package net.reportfoundry;

import net.reportfoundry.style.Alignment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ImageTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldRenderImage() throws Exception {
        Path imagePath = createTestImage();

        assertDoesNotThrow(() -> ReportFoundry.forge()
                .image(imagePath, 100, Alignment.LEFT)
                .toBytes()
        );
    }

    @Test
    void shouldRenderImageWithExplicitDimensions() throws Exception {
        Path imagePath = createTestImage();

        assertDoesNotThrow(() -> ReportFoundry.forge()
                .image(imagePath, 100, 50, Alignment.CENTER)
                .toBytes()
        );
    }

    private Path createTestImage() throws Exception {
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Path path = tempDirectory.resolve("test-image.png");

        ImageIO.write(image, "png", path.toFile());

        return path;
    }
}