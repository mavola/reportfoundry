package net.reportfoundry.style;

public class Style {

    private float fontSize = 12;
    private boolean bold = false;
    private Alignment alignment = Alignment.LEFT;

    public Style fontSize(float fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public Style bold() {
        this.bold = true;
        return this;
    }

    public Style align(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public float getFontSize() {
        return fontSize;
    }

    public boolean isBold() {
        return bold;
    }

    public Alignment getAlignment() {
        return alignment;
    }
}
