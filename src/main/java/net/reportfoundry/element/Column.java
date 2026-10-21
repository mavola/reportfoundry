package net.reportfoundry.element;

import net.reportfoundry.exception.ConfigurationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Column {

    private final float widthPercentage;
    private final List<Element> elements;

    public Column(float widthPercentage, List<Element> elements) {
        if (widthPercentage <= 0 || widthPercentage > 100) {
            throw new ConfigurationException("Column width percentage must be between 0 and 100.");
        }

        this.widthPercentage = widthPercentage;
        this.elements = new ArrayList<>(elements);
    }

    public float getWidthPercentage() {
        return widthPercentage;
    }

    public List<Element> getElements() {
        return Collections.unmodifiableList(elements);
    }
}