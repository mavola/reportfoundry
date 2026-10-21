package net.reportfoundry.element;

import net.reportfoundry.style.Style;

public class Heading implements Element {

    private final String content;
    private final Style style;

    public Heading(String content, float fontSize) {
        this.content = content;
        this.style = new Style().fontSize(fontSize).bold();
    }

    public String getContent() {
        return content;
    }

    public Style getStyle() {
        return style;
    }
}
