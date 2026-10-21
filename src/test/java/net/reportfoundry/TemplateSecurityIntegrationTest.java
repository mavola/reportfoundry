package net.reportfoundry;

import com.sun.net.httpserver.HttpServer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TemplateSecurityIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldNotRequestHttpResources() throws IOException {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

        server.createContext("/image.png", exchange -> {
            requests.incrementAndGet();
            byte[] response = "blocked".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);

            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });

        server.start();

        try {
            int port = server.getAddress().getPort();
            Path template = tempDir.resolve("report.html");

            Files.writeString(template, """
                    <html>
                    <body>
                        <h1>Network Security</h1>
                        <img src="http://127.0.0.1:%d/image.png"/>
                    </body>
                    </html>
                    """.formatted(port));

            byte[] pdf = ReportFoundry.template(template).toBytes();

            try (PDDocument document = Loader.loadPDF(pdf)) {
                assertEquals(1, document.getNumberOfPages());
                assertFalse(hasEmbeddedImage(document));
            }

            assertEquals(0, requests.get(), "The renderer must not request HTTP resources.");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldNotEmbedImageOutsideTemplateDirectory() throws IOException {
        Path templates = Files.createDirectory(tempDir.resolve("templates"));
        Path secret = tempDir.resolve("secret.png");
        createImage(secret, Color.RED);

        Path template = templates.resolve("report.html");

        Files.writeString(template, """
                <html>
                <body>
                    <h1>Filesystem Security</h1>
                    <img src="../secret.png"/>
                </body>
                </html>
                """);

        byte[] pdf = ReportFoundry.template(template).toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(1, document.getNumberOfPages());
            assertFalse(hasEmbeddedImage(document), "Images outside the template directory must not be embedded.");
        }
    }

    @Test
    void shouldNotEmbedExternalImageReferencedFromCss() throws IOException {
        Path templates = Files.createDirectory(tempDir.resolve("templates"));
        Path secret = tempDir.resolve("secret.png");
        createImage(secret, Color.RED);

        Path stylesheet = templates.resolve("report.css");

        Files.writeString(stylesheet, """
                .banner {
                    width: 100px;
                    height: 50px;
                    background-image: url('../secret.png');
                }
                """);

        Path template = templates.resolve("report.html");

        Files.writeString(template, """
                <html>
                <head>
                    <link rel="stylesheet" href="report.css"/>
                </head>
                <body>
                    <div class="banner">Protected Report</div>
                </body>
                </html>
                """);

        byte[] pdf = ReportFoundry.template(template).toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(1, document.getNumberOfPages());
            assertFalse(hasEmbeddedImage(document), "CSS must not load images outside the template directory.");
        }
    }

    @Test
    void shouldAllowImageReferencedFromCssInsideTemplateDirectory() throws IOException {
        Path templates = Files.createDirectory(tempDir.resolve("templates"));
        Path images = Files.createDirectory(templates.resolve("images"));
        createImage(images.resolve("logo.png"), Color.BLUE);

        Files.writeString(templates.resolve("report.css"), """
                .banner {
                    width: 100px;
                    height: 50px;
                    background-image: url('images/logo.png');
                    background-repeat: no-repeat;
                }
                """);

        Path template = templates.resolve("report.html");

        Files.writeString(template, """
                <html>
                <head>
                    <link rel="stylesheet" href="report.css"/>
                </head>
                <body>
                    <div class="banner">Allowed Resource</div>
                </body>
                </html>
                """);

        byte[] pdf = ReportFoundry.template(template).toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(1, document.getNumberOfPages());
            assertTrue(hasEmbeddedImage(document), "Images inside the template directory should be embedded.");
        }
    }

    private void createImage(Path path, Color color) throws IOException {
        BufferedImage image = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        try {
            graphics.setColor(color);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        } finally {
            graphics.dispose();
        }

        assertTrue(ImageIO.write(image, "png", path.toFile()));
    }

    private boolean hasEmbeddedImage(PDDocument document) throws IOException {
        for (int page = 0; page < document.getNumberOfPages(); page++) {
            for (COSName name : document.getPage(page).getResources().getXObjectNames()) {
                PDXObject object = document.getPage(page).getResources().getXObject(name);

                if (object instanceof PDImageXObject) {
                    return true;
                }
            }
        }

        return false;
    }
}