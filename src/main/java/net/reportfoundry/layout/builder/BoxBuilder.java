package net.reportfoundry.layout.builder;

import net.reportfoundry.element.Box;
import net.reportfoundry.element.Element;
import net.reportfoundry.element.Image;
import net.reportfoundry.element.Spacer;
import net.reportfoundry.element.Text;
import net.reportfoundry.style.Alignment;
import net.reportfoundry.style.BoxStyle;
import net.reportfoundry.style.Style;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BoxBuilder {

    private final List<Element> elements = new ArrayList<>();
    private final BoxStyle style = new BoxStyle();

    public BoxBuilder style(Consumer<BoxStyle> styleConfigurer) {
        styleConfigurer.accept(style);
        return this;
    }

    public BoxBuilder text(String text) {
        elements.add(new Text(text));
        return this;
    }

    public BoxBuilder text(String text, Consumer<Style> styleConfigurer) {
        Style textStyle = new Style();
        styleConfigurer.accept(textStyle);
        elements.add(new Text(text, textStyle));
        return this;
    }

    public BoxBuilder image(String path, float width) {
        return image(Path.of(path), width, Alignment.LEFT);
    }

    public BoxBuilder image(String path, float width, Alignment alignment) {
        return image(Path.of(path), width, alignment);
    }

    public BoxBuilder image(Path path, float width, Alignment alignment) {
        elements.add(new Image(path, width, alignment));
        return this;
    }

    public BoxBuilder space(float height) {
        elements.add(new Spacer(height));
        return this;
    }

    public Box build() {
        return new Box(elements, style);
    }
}