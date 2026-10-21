package net.reportfoundry;

import net.reportfoundry.document.Document;
import net.reportfoundry.document.Margins;
import net.reportfoundry.document.PageSize;
import net.reportfoundry.document.PageText;
import net.reportfoundry.element.Divider;
import net.reportfoundry.element.Heading;
import net.reportfoundry.element.Image;
import net.reportfoundry.element.PageBreak;
import net.reportfoundry.element.Spacer;
import net.reportfoundry.element.Text;
import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.layout.builder.BoxBuilder;
import net.reportfoundry.layout.builder.RowBuilder;
import net.reportfoundry.renderer.PdfBoxRenderer;
import net.reportfoundry.renderer.Renderer;
import net.reportfoundry.style.Alignment;
import net.reportfoundry.style.DividerStyle;
import net.reportfoundry.style.Style;
import net.reportfoundry.table.Table;
import net.reportfoundry.table.TableBuilder;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public final class ReportForge {

    private final Document document = new Document();

    ReportForge() {
    }

    public ReportForge page(PageSize pageSize) {
        document.setPageSize(pageSize);
        return this;
    }

    public ReportForge margins(float value) {
        document.setMargins(Margins.all(value));
        return this;
    }

    public ReportForge margins(float top, float right, float bottom, float left) {
        document.setMargins(new Margins(top, right, bottom, left));
        return this;
    }

    public ReportForge header(String text) {
        Style style = new Style().fontSize(9);
        document.setHeader(new PageText(text, style));
        return this;
    }

    public ReportForge header(String text, Consumer<Style> styleConfigurer) {
        Style style = new Style().fontSize(9);
        styleConfigurer.accept(style);
        document.setHeader(new PageText(text, style));
        return this;
    }

    public ReportForge footer(String text) {
        Style style = new Style().fontSize(9);
        document.setFooter(new PageText(text, style));
        return this;
    }

    public ReportForge footer(String text, Consumer<Style> styleConfigurer) {
        Style style = new Style().fontSize(9);
        styleConfigurer.accept(style);
        document.setFooter(new PageText(text, style));
        return this;
    }

    public ReportForge pageNumbers() {
        Style style = new Style().fontSize(9).align(Alignment.RIGHT);
        document.setPageNumbers(new PageText("Page {page} of {total}", style));
        return this;
    }

    public ReportForge pageNumbers(String pattern) {
        Style style = new Style().fontSize(9).align(Alignment.RIGHT);
        document.setPageNumbers(new PageText(pattern, style));
        return this;
    }

    public ReportForge pageNumbers(String pattern, Consumer<Style> styleConfigurer) {
        Style style = new Style().fontSize(9).align(Alignment.RIGHT);
        styleConfigurer.accept(style);
        document.setPageNumbers(new PageText(pattern, style));
        return this;
    }

    public ReportForge title(String title) {
        document.add(new Heading(title, 24));
        return this;
    }

    public ReportForge text(String text) {
        document.add(new Text(text));
        return this;
    }

    public ReportForge text(String text, Consumer<Style> styleConfigurer) {
        Style style = new Style();
        styleConfigurer.accept(style);
        document.add(new Text(text, style));
        return this;
    }

    public ReportForge image(String path, float width) {
        return image(Path.of(path), width, Alignment.LEFT);
    }

    public ReportForge image(String path, float width, Alignment alignment) {
        return image(Path.of(path), width, alignment);
    }

    public ReportForge image(Path path, float width, Alignment alignment) {
        document.add(new Image(path, width, alignment));
        return this;
    }

    public ReportForge image(String path, float width, float height) {
        return image(Path.of(path), width, height, Alignment.LEFT);
    }

    public ReportForge image(String path, float width, float height, Alignment alignment) {
        return image(Path.of(path), width, height, alignment);
    }

    public ReportForge image(Path path, float width, float height, Alignment alignment) {
        document.add(new Image(path, width, height, alignment));
        return this;
    }

    public ReportForge row(Consumer<RowBuilder> rowConfigurer) {
        RowBuilder builder = new RowBuilder();
        rowConfigurer.accept(builder);
        document.add(builder.build());
        return this;
    }

    public ReportForge box(Consumer<BoxBuilder> boxConfigurer) {
        BoxBuilder builder = new BoxBuilder();
        boxConfigurer.accept(builder);
        document.add(builder.build());
        return this;
    }

    public ReportForge divider() {
        document.add(new Divider(new DividerStyle()));
        return this;
    }

    public ReportForge divider(Consumer<DividerStyle> styleConfigurer) {
        DividerStyle style = new DividerStyle();
        styleConfigurer.accept(style);
        document.add(new Divider(style));
        return this;
    }

    public ReportForge pageBreak() {
        document.add(new PageBreak());
        return this;
    }

    public ReportForge space(float height) {
        document.add(new Spacer(height));
        return this;
    }

    public <T> ReportForge table(List<T> rows, Consumer<TableBuilder<T>> tableConfigurer) {
        TableBuilder<T> builder = new TableBuilder<>();
        tableConfigurer.accept(builder);

        if (builder.getColumns().isEmpty()) {
            throw new ConfigurationException("A table must contain at least one column.");
        }

        document.add(new Table<>(rows, builder.getColumns(), builder.getStyle()));
        return this;
    }

    public void export(String filename) {
        export(Path.of(filename));
    }

    public void export(Path path) {
        Renderer renderer = new PdfBoxRenderer();
        renderer.render(document, path);
    }

    public void export(OutputStream outputStream) {
        Renderer renderer = new PdfBoxRenderer();
        renderer.render(document, outputStream);
    }

    public byte[] toBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        export(outputStream);
        return outputStream.toByteArray();
    }
}