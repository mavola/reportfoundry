package net.reportfoundry.style;

import net.reportfoundry.exception.ConfigurationException;
public class BoxStyle {

    private RgbColor background = RgbColor.WHITE;
    private RgbColor borderColor = new RgbColor(180, 180, 180);
    private float borderWidth = 0.5f;
    private float padding = 10f;

    public BoxStyle background(RgbColor color) {
        if (color == null) {
            throw new ConfigurationException("Box background cannot be null.");
        }
        this.background = color;
        return this;
    }

    public BoxStyle borderColor(RgbColor color) {
        if (color == null) {
            throw new ConfigurationException("Box border color cannot be null.");
        }
        this.borderColor = color;
        return this;
    }

    public BoxStyle borderWidth(float width) {
        if (width < 0) {
            throw new ConfigurationException("Box border width cannot be negative.");
        }

        this.borderWidth = width;
        return this;
    }

    public BoxStyle padding(float padding) {
        if (padding < 0) {
            throw new ConfigurationException("Box padding cannot be negative.");
        }

        this.padding = padding;
        return this;
    }

    public RgbColor getBackground() {
        return background;
    }

    public RgbColor getBorderColor() {
        return borderColor;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public float getPadding() {
        return padding;
    }
}