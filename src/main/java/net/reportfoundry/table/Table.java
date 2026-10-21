package net.reportfoundry.table;

import net.reportfoundry.element.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Table<T> implements Element {

    private final List<T> rows;
    private final List<TableColumn<T>> columns;
    private final TableStyle style;

    public Table(List<T> rows, List<TableColumn<T>> columns, TableStyle style) {
        this.rows = new ArrayList<>(rows);
        this.columns = new ArrayList<>(columns);
        this.style = style;
    }

    public List<T> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public List<TableColumn<T>> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public TableStyle getStyle() {
        return style;
    }
}
