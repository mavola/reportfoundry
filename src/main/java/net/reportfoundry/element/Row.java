package net.reportfoundry.element;

import net.reportfoundry.exception.ConfigurationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Row implements Element {

    private final List<Column> columns;
    private final float gap;

    public Row(List<Column> columns, float gap) {
        if (columns == null || columns.isEmpty()) {
            throw new ConfigurationException("A row must contain at least one column.");
        }

        if (gap < 0) {
            throw new ConfigurationException("Row gap cannot be negative.");
        }

        float totalWidth = 0;

        for (Column column : columns) {
            totalWidth += column.getWidthPercentage();
        }

        if (Math.abs(totalWidth - 100f) > 0.001f) {
            throw new ConfigurationException("The sum of row column widths must equal 100%.");
        }

        this.columns = new ArrayList<>(columns);
        this.gap = gap;
    }

    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public float getGap() {
        return gap;
    }
}