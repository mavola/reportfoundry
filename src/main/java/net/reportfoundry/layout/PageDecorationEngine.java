package net.reportfoundry.layout;

import net.reportfoundry.exception.LayoutException;
import net.reportfoundry.document.Document;
import net.reportfoundry.document.Margins;
import net.reportfoundry.document.PageText;
import net.reportfoundry.style.Alignment;
import net.reportfoundry.style.RgbColor;
import net.reportfoundry.style.Style;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;

public class PageDecorationEngine {

    public void apply(Document document, LayoutResult layout, PDFont regularFont, PDFont boldFont) {
        int totalPages = layout.getPages().size();

        float pageWidth = document.getPageSize().getWidth();
        float pageHeight = document.getPageSize().getHeight();

        Margins margins = document.getMargins();

        float headerY = pageHeight - margins.top() / 2f;
        float footerY = margins.bottom() / 2f;

        for (int i = 0; i < totalPages; i++) {
            LayoutPage page = layout.getPages().get(i);
            int pageNumber = i + 1;

            if (document.hasHeader()) {
                addPageText(page, document.getHeader(), pageWidth, margins, headerY, regularFont, boldFont);
            }

            if (document.hasFooter()) {
                addPageText(page, document.getFooter(), pageWidth, margins, footerY, regularFont, boldFont);
            }

            if (document.hasPageNumbers()) {
                PageText pageNumbers = document.getPageNumbers();

                String text = pageNumbers.getContent()
                        .replace("{page}", String.valueOf(pageNumber))
                        .replace("{total}", String.valueOf(totalPages));

                PageText resolvedPageNumbers = new PageText(text, pageNumbers.getStyle());
                addPageText(page, resolvedPageNumbers, pageWidth, margins, footerY, regularFont, boldFont);
            }
        }
    }

    private void addPageText(LayoutPage page, PageText pageText, float pageWidth, Margins margins, float y, PDFont regularFont, PDFont boldFont) {
        Style style = pageText.getStyle();
        PDFont font = style.isBold() ? boldFont : regularFont;
        float x = calculateX(pageText.getContent(), font, style.getFontSize(), pageWidth, margins, style.getAlignment());
        page.add(new TextLine(pageText.getContent(), x, y, style.getFontSize(), style.isBold(), style.getAlignment(), RgbColor.BLACK));
    }

    private float calculateX(String text, PDFont font, float fontSize, float pageWidth, Margins margins, Alignment alignment) {
        float textWidth;

        try {
            textWidth = font.getStringWidth(text) / 1000f * fontSize;
        } catch (IOException e) {
            throw new LayoutException("Could not calculate page decoration text width.", e);
        }
        float availableWidth = pageWidth - margins.left() - margins.right();

        return switch (alignment) {
            case LEFT -> margins.left();
            case CENTER -> margins.left() + (availableWidth - textWidth) / 2;
            case RIGHT -> pageWidth - margins.right() - textWidth;
        };
    }
}