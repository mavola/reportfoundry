package net.reportfoundry.style;

import net.reportfoundry.exception.ConfigurationException;
public class DividerStyle {

    private RgbColor color = new RgbColor(180, 180, 180);
    private float thickness = 0.5f;
    private float widthPercentage = 100f;
    private Alignment alignment = Alignment.LEFT;

    public DividerStyle color(RgbColor color) {
        if (color == null) {
            throw new ConfigurationException("Divider color cannot be null.");
        }
        this.color = color;
        return this;
    }

    public DividerStyle thickness(float thickness) {
        if (thickness <= 0) {
            throw new ConfigurationException("Divider thickness must be greater than zero.");
        }
        this.thickness = thickness;
        return this;
    }

    public DividerStyle width(float widthPercentage) {
        if (widthPercentage <= 0 || widthPercentage > 100) {
            throw new ConfigurationException("Divider width must be between 0 and 100 percent.");
        }
        this.widthPercentage = widthPercentage;
        return this;
    }

    public DividerStyle align(Alignment alignment) {
        if (alignment == null) {
            throw new ConfigurationException("Divider alignment cannot be null.");
        }
        this.alignment = alignment;
        return this;
    }

    public RgbColor getColor() {
        return color;
    }

    public float getThickness() {
        return thickness;
    }

    public float getWidthPercentage() {
        return widthPercentage;
    }

    public Alignment getAlignment() {
        return alignment;
    }
}