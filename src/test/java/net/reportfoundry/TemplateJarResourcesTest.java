package net.reportfoundry;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class TemplateJarResourcesTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldLoadHtmlAndCssFromJar() throws IOException {
        Path jarPath = tempDir.resolve("templates.jar");

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <link rel="stylesheet" href="report.css"/>
                </head>
                <body>
                    <h1>First Page</h1>
                    <div class="second-page">
                        <h1>Second Page</h1>
                    </div>
                </body>
                </html>
                """;

        String css = """
                @page {
                    size: A4;
                    margin: 40px;
                }

                .second-page {
                    page-break-before: always;
                }
                """;

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarPath))) {
            addEntry(jar, "templates/jar/report.html", html);
            addEntry(jar, "templates/jar/report.css", css);
        }

        byte[] pdf = renderFromJar(jarPath, "templates/jar/report.html");

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(2, document.getNumberOfPages(), "The external CSS inside the JAR should be applied.");

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
    void shouldLoadImageFromJar() throws IOException {
        Path jarPath = tempDir.resolve("images.jar");

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                </head>
                <body>
                    <h1>Image Report</h1>
                    <img src="images/logo.png" width="120" height="60"/>
                </body>
                </html>
                """;

        BufferedImage image = new BufferedImage(120, 60, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        try {
            graphics.setColor(Color.BLUE);
            graphics.fillRect(0, 0, 120, 60);
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream imageOutput = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", imageOutput));

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarPath))) {
            addEntry(jar, "templates/jar/report.html", html);
            addEntry(jar, "templates/jar/images/logo.png", imageOutput.toByteArray());
        }

        byte[] pdf = renderFromJar(jarPath, "templates/jar/report.html");

        try (PDDocument document = Loader.loadPDF(pdf)) {
            boolean imageFound = false;

            for (COSName name : document.getPage(0).getResources().getXObjectNames()) {
                PDXObject object = document.getPage(0).getResources().getXObject(name);

                if (object instanceof PDImageXObject) {
                    imageFound = true;
                    break;
                }
            }

            assertTrue(imageFound, "The PDF should contain the PNG image loaded from the JAR.");
        }
    }

    @Test
    void shouldLoadCustomFontFromJar() throws IOException {
        Path jarPath = tempDir.resolve("fonts.jar");

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <link rel="stylesheet" href="report.css"/>
                </head>
                <body>
                    <h1>Custom Font Report</h1>
                    <p>Unicode test: á é í ó ú ñ</p>
                </body>
                </html>
                """;

        String css = """
                @font-face {
                    font-family: 'DejaVu Sans';
                    src: url('fonts/DejaVuSans.ttf');
                    font-weight: normal;
                    font-style: normal;
                }

                body {
                    font-family: 'DejaVu Sans';
                }
                """;

        byte[] fontBytes;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("fonts/DejaVuSans.ttf")) {
            assertNotNull(inputStream, "Missing test font: src/test/resources/fonts/DejaVuSans.ttf");
            fontBytes = inputStream.readAllBytes();
        }

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarPath))) {
            addEntry(jar, "templates/jar/report.html", html);
            addEntry(jar, "templates/jar/report.css", css);
            addEntry(jar, "templates/jar/fonts/DejaVuSans.ttf", fontBytes);
        }

        byte[] pdf = renderFromJar(jarPath, "templates/jar/report.html");

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);

            assertTrue(text.contains("Custom Font Report"));
            assertTrue(text.contains("á é í ó ú ñ"));

            boolean customFontFound = false;

            for (PDPageFontEntry entry : getPageFonts(document)) {
                if (entry.font().getName().contains("DejaVu")) {
                    customFontFound = true;
                    break;
                }
            }

            assertTrue(customFontFound, "The PDF should contain the embedded DejaVu font.");
        }
    }

    private byte[] renderFromJar(Path jarPath, String templatePath) throws IOException {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        URL jarUrl = new URL("jar:" + jarPath.toUri().toURL() + "!/");
        JarURLConnection connection = (JarURLConnection) jarUrl.openConnection();
        boolean originalCaching = connection.getDefaultUseCaches();
        connection.setDefaultUseCaches(false);

        try (URLClassLoader jarClassLoader = new URLClassLoader(new URL[]{jarPath.toUri().toURL()}, originalClassLoader)) {
            Thread.currentThread().setContextClassLoader(jarClassLoader);
            return ReportFoundry.template(templatePath).toBytes();
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            connection.setDefaultUseCaches(originalCaching);
        }
    }

    private java.util.List<PDPageFontEntry> getPageFonts(PDDocument document) throws IOException {
        java.util.List<PDPageFontEntry> fonts = new java.util.ArrayList<>();

        for (org.apache.pdfbox.pdmodel.PDPage page : document.getPages()) {
            for (COSName name : page.getResources().getFontNames()) {
                PDFont font = page.getResources().getFont(name);
                fonts.add(new PDPageFontEntry(font));
            }
        }

        return fonts;
    }

    private void addEntry(JarOutputStream jar, String name, String content) throws IOException {
        addEntry(jar, name, content.getBytes(StandardCharsets.UTF_8));
    }

    private void addEntry(JarOutputStream jar, String name, byte[] content) throws IOException {
        JarEntry entry = new JarEntry(name);
        jar.putNextEntry(entry);
        jar.write(content);
        jar.closeEntry();
    }

    private record PDPageFontEntry(PDFont font) {
    }
}