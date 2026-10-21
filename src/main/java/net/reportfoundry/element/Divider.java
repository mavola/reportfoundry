package net.reportfoundry.element;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.style.DividerStyle;

public class Divider implements Element {

    private final DividerStyle style;

    public Divider(DividerStyle style) {
        if (style == null) {
            throw new ConfigurationException("Divider style cannot be null.");
        }
        this.style = style;
    }

    public DividerStyle getStyle() {
        return style;
    }
}