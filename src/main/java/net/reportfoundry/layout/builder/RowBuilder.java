package net.reportfoundry.layout.builder;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.element.Column;
import net.reportfoundry.element.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RowBuilder {

    private final List<Column> columns = new ArrayList<>();
    private float gap = 0;

    public RowBuilder gap(float gap) {
        if (gap < 0) {
            throw new ConfigurationException("Row gap cannot be negative.");
        }
        this.gap = gap;
        return this;
    }

    public RowBuilder column(float widthPercentage, Consumer<ColumnBuilder> columnConfigurer) {
        ColumnBuilder builder = new ColumnBuilder();
        columnConfigurer.accept(builder);
        columns.add(new Column(widthPercentage, builder.getElements()));
        return this;
    }

    public Row build() {
        return new Row(columns, gap);
    }
}