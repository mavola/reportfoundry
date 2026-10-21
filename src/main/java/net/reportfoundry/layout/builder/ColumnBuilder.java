package net.reportfoundry.layout.builder;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.element.Element;
import net.reportfoundry.element.Image;
import net.reportfoundry.element.Spacer;
import net.reportfoundry.element.Text;
import net.reportfoundry.style.Alignment;
import net.reportfoundry.style.Style;
import net.reportfoundry.table.Table;
import net.reportfoundry.table.TableBuilder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ColumnBuilder {

    private final List<Element> elements = new ArrayList<>();

    public ColumnBuilder text(String text) {
        elements.add(new Text(text));
        return this;
    }

    public ColumnBuilder text(String text, Consumer<Style> styleConfigurer) {
        Style style = new Style();
        styleConfigurer.accept(style);
        elements.add(new Text(text, style));
        return this;
    }

    public ColumnBuilder image(String path, float width) {
        return image(Path.of(path), width, Alignment.LEFT);
    }

    public ColumnBuilder image(String path, float width, Alignment alignment) {
        return image(Path.of(path), width, alignment);
    }

    public ColumnBuilder image(Path path, float width, Alignment alignment) {
        elements.add(new Image(path, width, alignment));
        return this;
    }

    public ColumnBuilder image(String path, float width, float height, Alignment alignment) {
        elements.add(new Image(Path.of(path), width, height, alignment));
        return this;
    }

    public ColumnBuilder row(Consumer<RowBuilder> rowConfigurer) {
        RowBuilder builder = new RowBuilder();
        rowConfigurer.accept(builder);
        elements.add(builder.build());
        return this;
    }

    public <T> ColumnBuilder table(List<T> rows, Consumer<TableBuilder<T>> tableConfigurer) {
        TableBuilder<T> builder = new TableBuilder<>();
        tableConfigurer.accept(builder);

        if (builder.getColumns().isEmpty()) {
            throw new ConfigurationException("A table must contain at least one column.");
        }

        elements.add(new Table<>(rows, builder.getColumns(), builder.getStyle()));
        return this;
    }

    public ColumnBuilder space(float height) {
        elements.add(new Spacer(height));
        return this;
    }

    public List<Element> getElements() {
        return List.copyOf(elements);
    }
}