package net.reportfoundry.element;

import net.reportfoundry.style.Style;

public class Text implements Element {

    private final String content;
    private final Style style;

    public Text(String content) {
        this(content, new Style());
    }

    public Text(String content, Style style) {
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
