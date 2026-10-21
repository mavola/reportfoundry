package net.reportfoundry.element;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.style.BoxStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Box implements Element {

    private final List<Element> elements;
    private final BoxStyle style;

    public Box(List<Element> elements, BoxStyle style) {
        if (elements == null) {
            throw new ConfigurationException("Box elements cannot be null.");
        }

        if (style == null) {
            throw new ConfigurationException("Box style cannot be null.");
        }

        this.elements = new ArrayList<>(elements);
        this.style = style;
    }

    public List<Element> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public BoxStyle getStyle() {
        return style;
    }
}