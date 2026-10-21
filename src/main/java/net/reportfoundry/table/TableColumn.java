package net.reportfoundry.table;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.style.Alignment;

import java.util.function.Function;

public class TableColumn<T> {

    private final String header;
    private final Function<T, ?> valueExtractor;
    private final Float widthPercentage;
    private final Alignment alignment;

    public TableColumn(String header, Function<T, ?> valueExtractor) {
        this(header, valueExtractor, null, Alignment.LEFT);
    }

    public TableColumn(String header, Function<T, ?> valueExtractor, Float widthPercentage) {
        this(header, valueExtractor, widthPercentage, Alignment.LEFT);
    }

    public TableColumn(String header, Function<T, ?> valueExtractor, Float widthPercentage, Alignment alignment) {
        if (header == null) {
            throw new ConfigurationException("Table column header cannot be null.");
        }

        if (valueExtractor == null) {
            throw new ConfigurationException("Table column value extractor cannot be null.");
        }

        if (widthPercentage != null && widthPercentage <= 0) {
            throw new ConfigurationException("Column width percentage must be greater than zero.");
        }

        if (alignment == null) {
            throw new ConfigurationException("Column alignment cannot be null.");
        }

        this.header = header;
        this.valueExtractor = valueExtractor;
        this.widthPercentage = widthPercentage;
        this.alignment = alignment;
    }

    public String getHeader() {
        return header;
    }

    public String getValue(T row) {
        Object value = valueExtractor.apply(row);
        return value != null ? value.toString() : "";
    }

    public Float getWidthPercentage() {
        return widthPercentage;
    }

    public boolean hasExplicitWidth() {
        return widthPercentage != null;
    }

    public Alignment getAlignment() {
        return alignment;
    }
}