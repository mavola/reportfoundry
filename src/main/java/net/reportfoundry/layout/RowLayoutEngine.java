package net.reportfoundry.layout;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.exception.LayoutException;
import net.reportfoundry.document.Margins;
import net.reportfoundry.element.Column;
import net.reportfoundry.element.Element;
import net.reportfoundry.element.Image;
import net.reportfoundry.element.Row;
import net.reportfoundry.element.Spacer;
import net.reportfoundry.element.Text;
import net.reportfoundry.style.Alignment;
import net.reportfoundry.style.RgbColor;
import net.reportfoundry.style.Style;
import net.reportfoundry.table.Table;
import org.apache.pdfbox.pdmodel.font.PDFont;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RowLayoutEngine {

    private static final float LINE_HEIGHT_FACTOR = 1.5f;
    private static final float ELEMENT_SPACING = 10f;

    private final TableLayoutEngine tableLayoutEngine = new TableLayoutEngine();

    public RowLayoutResult layout(Row row, float topY, float pageWidth, Margins margins, PDFont regularFont, PDFont boldFont) {
        float availableWidth = pageWidth - margins.left() - margins.right();
        return layout(row, margins.left(), topY, availableWidth, regularFont, boldFont);
    }

    private RowLayoutResult layout(Row row, float startX, float topY, float availableWidth, PDFont regularFont, PDFont boldFont) {
        List<LayoutItem> items = new ArrayList<>();

        float totalGapWidth = row.getGap() * (row.getColumns().size() - 1);
        float columnsAvailableWidth = availableWidth - totalGapWidth;

        if (columnsAvailableWidth <= 0) {
            throw new LayoutException("Row gaps exceed the available row width.");
        }

        float currentX = startX;
        float maxHeight = 0;

        for (Column column : row.getColumns()) {
            float columnWidth = columnsAvailableWidth * column.getWidthPercentage() / 100f;
            float columnHeight = layoutColumn(column, items, currentX, topY, columnWidth, regularFont, boldFont);

            maxHeight = Math.max(maxHeight, columnHeight);
            currentX += columnWidth + row.getGap();
        }

        return new RowLayoutResult(items, maxHeight);
    }

    private float layoutColumn(Column column, List<LayoutItem> items, float columnX, float topY, float columnWidth, PDFont regularFont, PDFont boldFont) {
        float currentY = topY;

        for (Element element : column.getElements()) {
            if (element instanceof Text text) {
                Style style = text.getStyle();
                PDFont font = style.isBold() ? boldFont : regularFont;
                List<String> lines = wrapText(text.getContent(), font, style.getFontSize(), columnWidth);

                for (String line : lines) {
                    float lineHeight = style.getFontSize() * LINE_HEIGHT_FACTOR;
                    float x = calculateX(line, font, style.getFontSize(), columnX, columnWidth, style.getAlignment());

                    items.add(new TextLine(line, x, currentY, style.getFontSize(), style.isBold(), style.getAlignment(), RgbColor.BLACK));

                    currentY -= lineHeight;
                }

                currentY -= ELEMENT_SPACING;
            }

            if (element instanceof Image image) {
                float imageHeight = resolveImageHeight(image);

                if (image.getWidth() > columnWidth) {
                    throw new LayoutException("Image width exceeds the available column width.");
                }

                float x = calculateImageX(image.getWidth(), columnX, columnWidth, image.getAlignment());
                float y = currentY - imageHeight;

                items.add(new ImageBox(image.getPath(), x, y, image.getWidth(), imageHeight));
                currentY -= imageHeight + ELEMENT_SPACING;
            }

            if (element instanceof Row nestedRow) {
                RowLayoutResult nestedLayout = layout(nestedRow, columnX, currentY, columnWidth, regularFont, boldFont);

                items.addAll(nestedLayout.getItems());
                currentY -= nestedLayout.getHeight() + ELEMENT_SPACING;
            }

            if (element instanceof Table<?> table) {
                TableLayoutEngine.TableLayoutResult tableLayout = tableLayoutEngine.layout(table, columnX, currentY, columnWidth, regularFont, boldFont);

                items.addAll(tableLayout.items());
                currentY -= tableLayout.height() + ELEMENT_SPACING;
            }

            if (element instanceof Spacer spacer) {
                currentY -= spacer.height();
            }
        }

        return topY - currentY;
    }

    private float resolveImageHeight(Image image) {
        if (image.hasExplicitHeight()) {
            return image.getHeight();
        }

        if (!image.getPath().toFile().exists()) {
            throw new ConfigurationException("Image not found: " + image.getPath());
        }

        BufferedImage bufferedImage;

        try {
            bufferedImage = ImageIO.read(image.getPath().toFile());
        } catch (IOException e) {
            throw new ConfigurationException("Could not read image: " + image.getPath(), e);
        }

        if (bufferedImage == null) {
            throw new ConfigurationException("Unsupported or invalid image: " + image.getPath());
        }

        return image.getWidth() * bufferedImage.getHeight() / bufferedImage.getWidth();
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.isBlank()) {
            lines.add("");
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            float width = getTextWidth(candidate, font, fontSize);

            if (width <= maxWidth) {
                currentLine = new StringBuilder(candidate);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private float calculateX(String text, PDFont font, float fontSize, float columnX, float columnWidth, Alignment alignment) {
        float textWidth = getTextWidth(text, font, fontSize);

        return switch (alignment) {
            case LEFT -> columnX;
            case CENTER -> columnX + (columnWidth - textWidth) / 2;
            case RIGHT -> columnX + columnWidth - textWidth;
        };
    }

    private float calculateImageX(float imageWidth, float columnX, float columnWidth, Alignment alignment) {
        return switch (alignment) {
            case LEFT -> columnX;
            case CENTER -> columnX + (columnWidth - imageWidth) / 2;
            case RIGHT -> columnX + columnWidth - imageWidth;
        };
    }

    private float getTextWidth(String text, PDFont font, float fontSize) {
        try {
            return font.getStringWidth(text) / 1000f * fontSize;
        } catch (IOException e) {
            throw new LayoutException("Could not calculate text width.", e);
        }
    }
}