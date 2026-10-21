package net.reportfoundry.document;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.style.Style;

public class PageText {

    private final String content;
    private final Style style;

    public PageText(String content, Style style) {
        if (content == null) {
            throw new ConfigurationException("Page text content cannot be null.");
        }

        if (style == null) {
            throw new ConfigurationException("Page text style cannot be null.");
        }

        this.content = content;
        this.style = style;
    }

    public String getContent() {
        return content;
    }

    public Style getStyle() {
        return style;
    }
}