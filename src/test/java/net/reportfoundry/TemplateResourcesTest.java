package net.reportfoundry;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TemplateResourcesTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldLoadExternalCssFromFilesystem() throws IOException {
        Path template = tempDir.resolve("report.html");
        Path stylesheet = tempDir.resolve("report.css");

        Files.writeString(stylesheet, """
                @page {
                    size: A4;
                    margin: 40px;
                }

                .second-page {
                    page-break-before: always;
                }
                """);

        Files.writeString(template, """
                <html>
                <head>
                    <link rel="stylesheet" href="report.css"/>
                </head>
                <body>
                    <h1>First Page</h1>
                    <div class="second-page">
                        <h1>Second Page</h1>
                    </div>
                </body>
                </html>
                """);

        byte[] pdf = ReportFoundry.template(template).toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(2, document.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();

            stripper.setStartPage(1);
            stripper.setEndPage(1);
            assertTrue(stripper.getText(document).contains("First Page"));

            stripper.setStartPage(2);
            stripper.setEndPage(2);
            assertTrue(stripper.getText(document).contains("Second Page"));
        }
    }

    @Test
    void shouldLoadRelativeImageFromFilesystem() throws IOException {
        Path imagesDirectory = tempDir.resolve("images");
        Files.createDirectories(imagesDirectory);

        Path imagePath = imagesDirectory.resolve("logo.png");
        BufferedImage image = new BufferedImage(120, 60, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        try {
            graphics.setColor(Color.BLUE);
            graphics.fillRect(0, 0, 120, 60);
        } finally {
            graphics.dispose();
        }

        assertTrue(ImageIO.write(image, "png", imagePath.toFile()));

        Path template = tempDir.resolve("report.html");

        Files.writeString(template, """
                <html>
                <body>
                    <h1>Image Report</h1>
                    <img src="images/logo.png" width="120" height="60"/>
                </body>
                </html>
                """);

        byte[] pdf = ReportFoundry.template(template).toBytes();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(1, document.getNumberOfPages());

            boolean imageFound = false;

            for (org.apache.pdfbox.cos.COSName name : document.getPage(0).getResources().getXObjectNames()) {
                PDXObject object = document.getPage(0).getResources().getXObject(name);

                if (object instanceof PDImageXObject) {
                    imageFound = true;
                    break;
                }
            }

            assertTrue(imageFound, "The PDF should contain the external image.");
        }
    }
}