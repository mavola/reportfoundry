package net.reportfoundry.layout;

import net.reportfoundry.exception.LayoutException;
import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.style.Alignment;
import net.reportfoundry.table.Table;
import net.reportfoundry.table.TableColumn;
import net.reportfoundry.table.TableStyle;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TableLayoutEngine {

    private static final float TABLE_FONT_SIZE = 10f;
    private static final float TABLE_LINE_HEIGHT = TABLE_FONT_SIZE * 1.4f;

    public PreparedTableLayout prepare(Table<?> table, float availableWidth, PDFont regularFont, PDFont boldFont) {
        List<Float> columnWidths = calculateColumnWidths(table, availableWidth);
        float headerHeight = calculateHeaderHeight(table, columnWidths, boldFont);
        return new PreparedTableLayout(table, columnWidths, regularFont, boldFont, headerHeight);
    }

    public BlockLayout layoutHeader(PreparedTableLayout prepared, float startX, float topY) {
        Table<?> table = prepared.table();
        TableStyle style = table.getStyle();
        float padding = style.getCellPadding();
        float rowHeight = prepared.headerHeight();
        float bottomY = topY - rowHeight;
        float tableWidth = calculateTableWidth(prepared.columnWidths());

        List<LayoutItem> items = new ArrayList<>();

        items.add(new FilledRectangle(startX, bottomY, tableWidth, rowHeight, style.getHeaderBackground()));
        drawRowBorders(items, startX, topY, bottomY, prepared.columnWidths(), style);

        float currentX = startX;

        for (int i = 0; i < table.getColumns().size(); i++) {
            TableColumn<?> column = table.getColumns().get(i);
            float columnWidth = prepared.columnWidths().get(i);
            float availableCellWidth = columnWidth - padding * 2;

            List<String> lines = wrapText(column.getHeader(), prepared.boldFont(), TABLE_FONT_SIZE, availableCellWidth);
            float textY = topY - padding - TABLE_FONT_SIZE;

            for (String line : lines) {
                float textX = calculateCellX(line, prepared.boldFont(), TABLE_FONT_SIZE, currentX, columnWidth, Alignment.LEFT, padding);
                items.add(new TextLine(line, textX, textY, TABLE_FONT_SIZE, true, Alignment.LEFT, style.getHeaderTextColor()));
                textY -= TABLE_LINE_HEIGHT;
            }

            currentX += columnWidth;
        }

        return new BlockLayout(items, rowHeight);
    }

    public BlockLayout layoutRow(PreparedTableLayout prepared, Object row, int rowIndex, float startX, float topY) {
        return layoutRowInternal(prepared, row, rowIndex, startX, topY);
    }

    public TableLayoutResult layout(Table<?> table, float startX, float topY, float availableWidth, PDFont regularFont, PDFont boldFont) {
        PreparedTableLayout prepared = prepare(table, availableWidth, regularFont, boldFont);
        List<LayoutItem> items = new ArrayList<>();

        BlockLayout header = layoutHeader(prepared, startX, topY);
        items.addAll(header.items());

        float currentY = topY - header.height();
        int rowIndex = 0;

        for (Object row : table.getRows()) {
            BlockLayout rowLayout = layoutRow(prepared, row, rowIndex, startX, currentY);
            items.addAll(rowLayout.items());

            currentY -= rowLayout.height();
            rowIndex++;
        }

        return new TableLayoutResult(items, topY - currentY);
    }

    @SuppressWarnings("unchecked")
    private <T> BlockLayout layoutRowInternal(PreparedTableLayout prepared, Object rawRow, int rowIndex, float startX, float topY) {
        Table<T> table = (Table<T>) prepared.table();
        T row = (T) rawRow;

        TableStyle style = table.getStyle();
        float padding = style.getCellPadding();
        float rowHeight = calculateRowHeight(table, row, prepared.columnWidths(), prepared.regularFont());
        float bottomY = topY - rowHeight;
        float tableWidth = calculateTableWidth(prepared.columnWidths());

        List<LayoutItem> items = new ArrayList<>();

        if (style.hasAlternateRowBackground() && rowIndex % 2 == 1) {
            items.add(new FilledRectangle(startX, bottomY, tableWidth, rowHeight, style.getAlternateRowBackground()));
        }

        drawRowBorders(items, startX, topY, bottomY, prepared.columnWidths(), style);

        float currentX = startX;

        for (int i = 0; i < table.getColumns().size(); i++) {
            TableColumn<T> column = table.getColumns().get(i);
            float columnWidth = prepared.columnWidths().get(i);
            float availableCellWidth = columnWidth - padding * 2;

            String value = column.getValue(row);
            List<String> lines = wrapText(value, prepared.regularFont(), TABLE_FONT_SIZE, availableCellWidth);
            float textY = topY - padding - TABLE_FONT_SIZE;

            for (String line : lines) {
                float textX = calculateCellX(line, prepared.regularFont(), TABLE_FONT_SIZE, currentX, columnWidth, column.getAlignment(), padding);
                items.add(new TextLine(line, textX, textY, TABLE_FONT_SIZE, false, column.getAlignment(), style.getRowTextColor()));
                textY -= TABLE_LINE_HEIGHT;
            }

            currentX += columnWidth;
        }

        return new BlockLayout(items, rowHeight);
    }

    private List<Float> calculateColumnWidths(Table<?> table, float availableWidth) {
        float explicitPercentage = 0f;
        int automaticColumns = 0;

        for (TableColumn<?> column : table.getColumns()) {
            if (column.hasExplicitWidth()) {
                explicitPercentage += column.getWidthPercentage();
            } else {
                automaticColumns++;
            }
        }

        if (explicitPercentage > 100f) {
            throw new ConfigurationException("The sum of table column widths cannot exceed 100%.");
        }

        if (automaticColumns == 0 && Math.abs(explicitPercentage - 100f) > 0.001f) {
            throw new ConfigurationException("When every table column has an explicit width, the total must equal 100%.");
        }

        float remainingPercentage = 100f - explicitPercentage;
        float automaticPercentage = automaticColumns > 0 ? remainingPercentage / automaticColumns : 0f;

        List<Float> widths = new ArrayList<>();

        for (TableColumn<?> column : table.getColumns()) {
            float percentage = column.hasExplicitWidth() ? column.getWidthPercentage() : automaticPercentage;
            widths.add(availableWidth * percentage / 100f);
        }

        return widths;
    }

    private float calculateHeaderHeight(Table<?> table, List<Float> columnWidths, PDFont font) {
        float padding = table.getStyle().getCellPadding();
        int maxLines = 1;

        for (int i = 0; i < table.getColumns().size(); i++) {
            TableColumn<?> column = table.getColumns().get(i);
            float availableCellWidth = columnWidths.get(i) - padding * 2;

            List<String> lines = wrapText(column.getHeader(), font, TABLE_FONT_SIZE, availableCellWidth);
            maxLines = Math.max(maxLines, lines.size());
        }

        return maxLines * TABLE_LINE_HEIGHT + padding * 2;
    }

    private <T> float calculateRowHeight(Table<T> table, T row, List<Float> columnWidths, PDFont font) {
        float padding = table.getStyle().getCellPadding();
        int maxLines = 1;

        for (int i = 0; i < table.getColumns().size(); i++) {
            TableColumn<T> column = table.getColumns().get(i);
            String value = column.getValue(row);
            float availableCellWidth = columnWidths.get(i) - padding * 2;

            List<String> lines = wrapText(value, font, TABLE_FONT_SIZE, availableCellWidth);
            maxLines = Math.max(maxLines, lines.size());
        }

        return maxLines * TABLE_LINE_HEIGHT + padding * 2;
    }

    private void drawRowBorders(List<LayoutItem> items, float leftX, float topY, float bottomY, List<Float> columnWidths, TableStyle style) {
        float rightX = leftX + calculateTableWidth(columnWidths);

        items.add(new Line(leftX, topY, rightX, topY, style.getBorderWidth(), style.getBorderColor()));
        items.add(new Line(leftX, bottomY, rightX, bottomY, style.getBorderWidth(), style.getBorderColor()));

        float currentX = leftX;

        items.add(new Line(currentX, topY, currentX, bottomY, style.getBorderWidth(), style.getBorderColor()));

        for (float columnWidth : columnWidths) {
            currentX += columnWidth;
            items.add(new Line(currentX, topY, currentX, bottomY, style.getBorderWidth(), style.getBorderColor()));
        }
    }

    private float calculateTableWidth(List<Float> columnWidths) {
        float width = 0f;

        for (float columnWidth : columnWidths) {
            width += columnWidth;
        }

        return width;
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

    private float calculateCellX(String text, PDFont font, float fontSize, float cellX, float cellWidth, Alignment alignment, float padding) {
        float textWidth = getTextWidth(text, font, fontSize);

        return switch (alignment) {
            case LEFT -> cellX + padding;
            case CENTER -> cellX + (cellWidth - textWidth) / 2;
            case RIGHT -> cellX + cellWidth - padding - textWidth;
        };
    }

    private float getTextWidth(String text, PDFont font, float fontSize) {
        try {
            return font.getStringWidth(text) / 1000f * fontSize;
        } catch (IOException e) {
            throw new LayoutException("Could not calculate text width.", e);
        }
    }

    public record PreparedTableLayout(Table<?> table, List<Float> columnWidths, PDFont regularFont, PDFont boldFont, float headerHeight) {
        public PreparedTableLayout {
            columnWidths = List.copyOf(columnWidths);
        }
    }

    public record BlockLayout(List<LayoutItem> items, float height) {
        public BlockLayout {
            items = List.copyOf(items);
        }
    }

    public record TableLayoutResult(List<LayoutItem> items, float height) {
        public TableLayoutResult {
            items = List.copyOf(items);
        }
    }
}