package net.reportfoundry.renderer;

import net.reportfoundry.document.Document;
import net.reportfoundry.document.PageSize;
import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.exception.RenderException;
import net.reportfoundry.layout.FilledRectangle;
import net.reportfoundry.layout.ImageBox;
import net.reportfoundry.layout.LayoutEngine;
import net.reportfoundry.layout.LayoutItem;
import net.reportfoundry.layout.LayoutPage;
import net.reportfoundry.layout.LayoutResult;
import net.reportfoundry.layout.Line;
import net.reportfoundry.layout.PageDecorationEngine;
import net.reportfoundry.layout.TextLine;
import net.reportfoundry.style.RgbColor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PdfBoxRenderer implements Renderer {

    @Override
    public void render(Document document, Path path) {
        if (path == null) {
            throw new ConfigurationException("Output path cannot be null.");
        }

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            render(document, outputStream);
        } catch (RenderException | IOException e) {
            throw new RenderException("Could not write PDF to: " + path, e);
        }
    }

    @Override
    public void render(Document document, OutputStream outputStream) {
        if (document == null) {
            throw new ConfigurationException("Document cannot be null.");
        }

        if (outputStream == null) {
            throw new ConfigurationException("Output stream cannot be null.");
        }

        try (PDDocument pdfDocument = new PDDocument()) {
            renderDocument(document, pdfDocument);
            pdfDocument.save(outputStream);
        } catch (IOException e) {
            throw new RenderException("Could not render PDF.", e);
        }
    }

    private void renderDocument(Document document, PDDocument pdfDocument) throws IOException {
        PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        LayoutEngine layoutEngine = new LayoutEngine();
        LayoutResult layout = layoutEngine.layout(document, regularFont, boldFont);

        PageDecorationEngine pageDecorationEngine = new PageDecorationEngine();
        pageDecorationEngine.apply(document, layout, regularFont, boldFont);

        PageSize pageSize = document.getPageSize();
        PDRectangle rectangle = new PDRectangle(pageSize.getWidth(), pageSize.getHeight());

        for (LayoutPage layoutPage : layout.getPages()) {
            PDPage pdfPage = new PDPage(rectangle);
            pdfDocument.addPage(pdfPage);

            try (PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, pdfPage)) {
                for (LayoutItem item : layoutPage.getItems()) {
                    if (item instanceof FilledRectangle rectangleItem) {
                        drawFilledRectangle(contentStream, rectangleItem);
                    }

                    if (item instanceof Line line) {
                        drawLine(contentStream, line);
                    }

                    if (item instanceof ImageBox image) {
                        drawImage(pdfDocument, contentStream, image);
                    }

                    if (item instanceof TextLine line) {
                        PDType1Font font = line.bold() ? boldFont : regularFont;
                        drawText(contentStream, line.text(), font, line.fontSize(), line.x(), line.y(), line.color());
                    }
                }
            }
        }
    }

    private void drawText(PDPageContentStream contentStream, String text, PDType1Font font, float fontSize, float x, float y, RgbColor color) throws IOException {
        setNonStrokingColor(contentStream, color);
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawLine(PDPageContentStream contentStream, Line line) throws IOException {
        setStrokingColor(contentStream, line.color());
        contentStream.setLineWidth(line.width());
        contentStream.moveTo(line.x1(), line.y1());
        contentStream.lineTo(line.x2(), line.y2());
        contentStream.stroke();
    }

    private void drawFilledRectangle(PDPageContentStream contentStream, FilledRectangle rectangle) throws IOException {
        setNonStrokingColor(contentStream, rectangle.color());
        contentStream.addRect(rectangle.x(), rectangle.y(), rectangle.width(), rectangle.height());
        contentStream.fill();
    }

    private void drawImage(PDDocument document, PDPageContentStream contentStream, ImageBox image) throws IOException {
        if (!image.path().toFile().exists()) {
            throw new ConfigurationException("Image not found: " + image.path());
        }

        PDImageXObject pdfImage = PDImageXObject.createFromFileByContent(image.path().toFile(), document);
        contentStream.drawImage(pdfImage, image.x(), image.y(), image.width(), image.height());
    }

    private void setNonStrokingColor(PDPageContentStream contentStream, RgbColor color) throws IOException {
        contentStream.setNonStrokingColor(normalize(color.red()), normalize(color.green()), normalize(color.blue()));
    }

    private void setStrokingColor(PDPageContentStream contentStream, RgbColor color) throws IOException {
        contentStream.setStrokingColor(normalize(color.red()), normalize(color.green()), normalize(color.blue()));
    }

    private float normalize(int value) {
        return value / 255f;
    }
}