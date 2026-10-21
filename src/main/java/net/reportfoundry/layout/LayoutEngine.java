package net.reportfoundry.layout;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.exception.LayoutException;
import net.reportfoundry.document.Document;
import net.reportfoundry.document.Margins;
import net.reportfoundry.document.PageSize;
import net.reportfoundry.element.Box;
import net.reportfoundry.element.Divider;
import net.reportfoundry.element.Element;
import net.reportfoundry.element.Heading;
import net.reportfoundry.element.Image;
import net.reportfoundry.element.PageBreak;
import net.reportfoundry.element.Row;
import net.reportfoundry.element.Spacer;
import net.reportfoundry.element.Text;
import net.reportfoundry.style.Alignment;
import net.reportfoundry.style.DividerStyle;
import net.reportfoundry.style.RgbColor;
import net.reportfoundry.style.Style;
import net.reportfoundry.table.Table;
import org.apache.pdfbox.pdmodel.font.PDFont;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LayoutEngine {

    private static final float LINE_HEIGHT_FACTOR = 1.5f;
    private static final float ELEMENT_SPACING = 10f;

    private final RowLayoutEngine rowLayoutEngine = new RowLayoutEngine();
    private final TableLayoutEngine tableLayoutEngine = new TableLayoutEngine();
    private final BoxLayoutEngine boxLayoutEngine = new BoxLayoutEngine();

    public LayoutResult layout(Document document, PDFont regularFont, PDFont boldFont) {
        LayoutResult result = new LayoutResult();

        PageSize pageSize = document.getPageSize();
        Margins margins = document.getMargins();

        float pageWidth = pageSize.getWidth();
        float pageHeight = pageSize.getHeight();
        float availableWidth = pageWidth - margins.left() - margins.right();
        float usablePageHeight = pageHeight - margins.top() - margins.bottom();
        float currentY = pageHeight - margins.top();

        LayoutPage currentPage = new LayoutPage();
        result.addPage(currentPage);

        for (Element element : document.getElements()) {
            if (element instanceof PageBreak) {
                currentPage = createPage(result);
                currentY = pageHeight - margins.top();
                continue;
            }

            if (element instanceof Heading heading) {
                Style style = heading.getStyle();
                PDFont font = style.isBold() ? boldFont : regularFont;
                List<String> lines = wrapText(heading.getContent(), font, style.getFontSize(), availableWidth);

                for (String line : lines) {
                    float lineHeight = style.getFontSize() * LINE_HEIGHT_FACTOR;

                    if (!fitsOnPage(currentY, lineHeight, margins)) {
                        currentPage = createPage(result);
                        currentY = pageHeight - margins.top();
                    }

                    float x = calculateX(line, font, style.getFontSize(), pageWidth, margins, style.getAlignment());
                    currentPage.add(new TextLine(line, x, currentY, style.getFontSize(), style.isBold(), style.getAlignment(), RgbColor.BLACK));
                    currentY -= lineHeight;
                }

                currentY -= ELEMENT_SPACING;
            }

            if (element instanceof Text text) {
                Style style = text.getStyle();
                PDFont font = style.isBold() ? boldFont : regularFont;
                List<String> lines = wrapText(text.getContent(), font, style.getFontSize(), availableWidth);

                for (String line : lines) {
                    float lineHeight = style.getFontSize() * LINE_HEIGHT_FACTOR;

                    if (!fitsOnPage(currentY, lineHeight, margins)) {
                        currentPage = createPage(result);
                        currentY = pageHeight - margins.top();
                    }

                    float x = calculateX(line, font, style.getFontSize(), pageWidth, margins, style.getAlignment());
                    currentPage.add(new TextLine(line, x, currentY, style.getFontSize(), style.isBold(), style.getAlignment(), RgbColor.BLACK));
                    currentY -= lineHeight;
                }

                currentY -= ELEMENT_SPACING;
            }

            if (element instanceof Image image) {
                float imageHeight = resolveImageHeight(image);

                if (image.getWidth() > availableWidth) {
                    throw new LayoutException("Image width exceeds the available page width.");
                }

                if (imageHeight > usablePageHeight) {
                    throw new LayoutException("Image height exceeds the available page height.");
                }

                if (!fitsOnPage(currentY, imageHeight, margins)) {
                    currentPage = createPage(result);
                    currentY = pageHeight - margins.top();
                }

                float x = calculateBlockX(image.getWidth(), pageWidth, margins, image.getAlignment());
                float y = currentY - imageHeight;

                currentPage.add(new ImageBox(image.getPath(), x, y, image.getWidth(), imageHeight));
                currentY -= imageHeight + ELEMENT_SPACING;
            }

            if (element instanceof Row row) {
                RowLayoutResult rowLayout = rowLayoutEngine.layout(row, currentY, pageWidth, margins, regularFont, boldFont);

                if (rowLayout.getHeight() > usablePageHeight) {
                    throw new LayoutException("Row height exceeds the available page height.");
                }

                if (!fitsOnPage(currentY, rowLayout.getHeight(), margins)) {
                    currentPage = createPage(result);
                    currentY = pageHeight - margins.top();
                    rowLayout = rowLayoutEngine.layout(row, currentY, pageWidth, margins, regularFont, boldFont);
                }

                addItems(currentPage, rowLayout.getItems());
                currentY -= rowLayout.getHeight() + ELEMENT_SPACING;
            }

            if (element instanceof Box box) {
                BoxLayoutResult boxLayout = boxLayoutEngine.layout(box, margins.left(), currentY, availableWidth, regularFont, boldFont);

                if (boxLayout.height() > usablePageHeight) {
                    throw new LayoutException("Box height exceeds the available page height.");
                }

                if (!fitsOnPage(currentY, boxLayout.height(), margins)) {
                    currentPage = createPage(result);
                    currentY = pageHeight - margins.top();
                    boxLayout = boxLayoutEngine.layout(box, margins.left(), currentY, availableWidth, regularFont, boldFont);
                }

                addItems(currentPage, boxLayout.items());
                currentY -= boxLayout.height() + ELEMENT_SPACING;
            }

            if (element instanceof Divider divider) {
                DividerStyle style = divider.getStyle();
                float dividerWidth = availableWidth * style.getWidthPercentage() / 100f;
                float x = calculateBlockX(dividerWidth, pageWidth, margins, style.getAlignment());

                currentPage.add(new Line(x, currentY, x + dividerWidth, currentY, style.getThickness(), style.getColor()));
                currentY -= ELEMENT_SPACING;
            }

            if (element instanceof Table<?> table) {
                TableLayoutEngine.PreparedTableLayout prepared = tableLayoutEngine.prepare(table, availableWidth, regularFont, boldFont);

                if (prepared.headerHeight() > usablePageHeight) {
                    throw new LayoutException("Table header exceeds the available page height.");
                }

                TableLayoutEngine.BlockLayout header = tableLayoutEngine.layoutHeader(prepared, margins.left(), currentY);

                if (!fitsOnPage(currentY, header.height(), margins)) {
                    currentPage = createPage(result);
                    currentY = pageHeight - margins.top();
                    header = tableLayoutEngine.layoutHeader(prepared, margins.left(), currentY);
                }

                addItems(currentPage, header.items());
                currentY -= header.height();

                int rowIndex = 0;

                for (Object row : table.getRows()) {
                    TableLayoutEngine.BlockLayout rowLayout = tableLayoutEngine.layoutRow(prepared, row, rowIndex, margins.left(), currentY);

                    if (rowLayout.height() + prepared.headerHeight() > usablePageHeight) {
                        throw new LayoutException("Table row exceeds the available page height.");
                    }

                    if (!fitsOnPage(currentY, rowLayout.height(), margins)) {
                        currentPage = createPage(result);
                        currentY = pageHeight - margins.top();

                        header = tableLayoutEngine.layoutHeader(prepared, margins.left(), currentY);
                        addItems(currentPage, header.items());
                        currentY -= header.height();

                        rowLayout = tableLayoutEngine.layoutRow(prepared, row, rowIndex, margins.left(), currentY);
                    }

                    addItems(currentPage, rowLayout.items());
                    currentY -= rowLayout.height();
                    rowIndex++;
                }

                currentY -= ELEMENT_SPACING;
            }

            if (element instanceof Spacer spacer) {
                if (!fitsOnPage(currentY, spacer.height(), margins)) {
                    currentPage = createPage(result);
                    currentY = pageHeight - margins.top();
                }

                currentY -= spacer.height();
            }
        }

        return result;
    }

    private void addItems(LayoutPage page, List<LayoutItem> items) {
        for (LayoutItem item : items) {
            page.add(item);
        }
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

    private LayoutPage createPage(LayoutResult result) {
        LayoutPage page = new LayoutPage();
        result.addPage(page);
        return page;
    }

    private boolean fitsOnPage(float currentY, float requiredHeight, Margins margins) {
        return currentY - requiredHeight >= margins.bottom();
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

    private float getTextWidth(String text, PDFont font, float fontSize) {
        try {
            return font.getStringWidth(text) / 1000f * fontSize;
        } catch (IOException e) {
            throw new LayoutException("Could not calculate text width.", e);
        }
    }

    private float calculateX(String text, PDFont font, float fontSize, float pageWidth, Margins margins, Alignment alignment) {
        float textWidth = getTextWidth(text, font, fontSize);
        float availableWidth = pageWidth - margins.left() - margins.right();

        return switch (alignment) {
            case LEFT -> margins.left();
            case CENTER -> margins.left() + (availableWidth - textWidth) / 2;
            case RIGHT -> pageWidth - margins.right() - textWidth;
        };
    }

    private float calculateBlockX(float blockWidth, float pageWidth, Margins margins, Alignment alignment) {
        float availableWidth = pageWidth - margins.left() - margins.right();

        return switch (alignment) {
            case LEFT -> margins.left();
            case CENTER -> margins.left() + (availableWidth - blockWidth) / 2;
            case RIGHT -> pageWidth - margins.right() - blockWidth;
        };
    }
}