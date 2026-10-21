package net.reportfoundry.layout;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.exception.LayoutException;
import net.reportfoundry.element.Box;
import net.reportfoundry.element.Element;
import net.reportfoundry.element.Image;
import net.reportfoundry.element.Spacer;
import net.reportfoundry.element.Text;
import net.reportfoundry.style.Alignment;
import net.reportfoundry.style.BoxStyle;
import net.reportfoundry.style.RgbColor;
import net.reportfoundry.style.Style;
import org.apache.pdfbox.pdmodel.font.PDFont;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BoxLayoutEngine {

    private static final float LINE_HEIGHT_FACTOR = 1.5f;
    private static final float ELEMENT_SPACING = 10f;

    public BoxLayoutResult layout(Box box, float startX, float topY, float availableWidth, PDFont regularFont, PDFont boldFont) {
        BoxStyle boxStyle = box.getStyle();
        float padding = boxStyle.getPadding();
        float contentWidth = availableWidth - padding * 2;

        if (contentWidth <= 0) {
            throw new LayoutException("Box padding leaves no available content width.");
        }

        List<LayoutItem> contentItems = new ArrayList<>();
        float currentY = topY - padding;

        for (Element element : box.getElements()) {
            if (element instanceof Text text) {
                Style style = text.getStyle();
                PDFont font = style.isBold() ? boldFont : regularFont;
                List<String> lines = wrapText(text.getContent(), font, style.getFontSize(), contentWidth);

                for (String line : lines) {
                    float lineHeight = style.getFontSize() * LINE_HEIGHT_FACTOR;
                    float x = calculateX(line, font, style.getFontSize(), startX + padding, contentWidth, style.getAlignment());
                    contentItems.add(new TextLine(line, x, currentY, style.getFontSize(), style.isBold(), style.getAlignment(), RgbColor.BLACK));
                    currentY -= lineHeight;
                }

                currentY -= ELEMENT_SPACING;
            }

            if (element instanceof Image image) {
                float imageHeight = resolveImageHeight(image);

                if (image.getWidth() > contentWidth) {
                    throw new LayoutException("Image width exceeds the available box width.");
                }

                float x = calculateBlockX(image.getWidth(), startX + padding, contentWidth, image.getAlignment());
                float y = currentY - imageHeight;

                contentItems.add(new ImageBox(image.getPath(), x, y, image.getWidth(), imageHeight));
                currentY -= imageHeight + ELEMENT_SPACING;
            }

            if (element instanceof Spacer spacer) {
                currentY -= spacer.height();
            }
        }

        float contentHeight = topY - padding - currentY;

        if (contentHeight > 0) {
            contentHeight -= ELEMENT_SPACING;
        }

        float boxHeight = contentHeight + padding * 2;
        float bottomY = topY - boxHeight;

        List<LayoutItem> items = new ArrayList<>();

        items.add(new FilledRectangle(startX, bottomY, availableWidth, boxHeight, boxStyle.getBackground()));

        if (boxStyle.getBorderWidth() > 0) {
            addBorder(items, startX, topY, availableWidth, boxHeight, boxStyle);
        }

        items.addAll(contentItems);

        return new BoxLayoutResult(items, boxHeight);
    }

    private void addBorder(List<LayoutItem> items, float x, float topY, float width, float height, BoxStyle style) {
        float bottomY = topY - height;
        float rightX = x + width;

        items.add(new Line(x, topY, rightX, topY, style.getBorderWidth(), style.getBorderColor()));
        items.add(new Line(x, bottomY, rightX, bottomY, style.getBorderWidth(), style.getBorderColor()));
        items.add(new Line(x, topY, x, bottomY, style.getBorderWidth(), style.getBorderColor()));
        items.add(new Line(rightX, topY, rightX, bottomY, style.getBorderWidth(), style.getBorderColor()));
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

    private float calculateX(String text, PDFont font, float fontSize, float startX, float availableWidth, Alignment alignment) {
        float textWidth = getTextWidth(text, font, fontSize);

        return switch (alignment) {
            case LEFT -> startX;
            case CENTER -> startX + (availableWidth - textWidth) / 2;
            case RIGHT -> startX + availableWidth - textWidth;
        };
    }

    private float calculateBlockX(float blockWidth, float startX, float availableWidth, Alignment alignment) {
        return switch (alignment) {
            case LEFT -> startX;
            case CENTER -> startX + (availableWidth - blockWidth) / 2;
            case RIGHT -> startX + availableWidth - blockWidth;
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