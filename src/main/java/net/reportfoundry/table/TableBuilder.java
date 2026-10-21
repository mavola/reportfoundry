package net.reportfoundry.table;

import net.reportfoundry.style.Alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TableBuilder<T> {

    private final List<TableColumn<T>> columns = new ArrayList<>();
    private final TableStyle style = new TableStyle();

    public TableBuilder<T> column(String header, Function<T, ?> valueExtractor) {
        columns.add(new TableColumn<>(header, valueExtractor));
        return this;
    }

    public TableBuilder<T> column(String header, Function<T, ?> valueExtractor, float widthPercentage) {
        columns.add(new TableColumn<>(header, valueExtractor, widthPercentage));
        return this;
    }

    public TableBuilder<T> column(String header, Function<T, ?> valueExtractor, float widthPercentage, Alignment alignment) {
        columns.add(new TableColumn<>(header, valueExtractor, widthPercentage, alignment));
        return this;
    }

    public TableBuilder<T> style(Consumer<TableStyle> styleConfigurer) {
        styleConfigurer.accept(style);
        return this;
    }

    public List<TableColumn<T>> getColumns() {
        return List.copyOf(columns);
    }

    public TableStyle getStyle() {
        return style;
    }
}
