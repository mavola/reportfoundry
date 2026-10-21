package net.reportfoundry.table;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.style.RgbColor;

public class TableStyle {

    private RgbColor headerBackground = RgbColor.LIGHT_GRAY;
    private RgbColor headerTextColor = RgbColor.BLACK;
    private RgbColor rowTextColor = RgbColor.BLACK;
    private RgbColor borderColor = RgbColor.BLACK;
    private RgbColor alternateRowBackground;
    private float borderWidth = 0.5f;
    private float cellPadding = 6f;

    public TableStyle headerBackground(RgbColor color) {
        this.headerBackground = color;
        return this;
    }

    public TableStyle headerTextColor(RgbColor color) {
        this.headerTextColor = color;
        return this;
    }

    public TableStyle rowTextColor(RgbColor color) {
        this.rowTextColor = color;
        return this;
    }

    public TableStyle borderColor(RgbColor color) {
        this.borderColor = color;
        return this;
    }

    public TableStyle alternateRowBackground(RgbColor color) {
        this.alternateRowBackground = color;
        return this;
    }

    public TableStyle borderWidth(float width) {
        if (width < 0) {
            throw new ConfigurationException("Border width cannot be negative.");
        }

        this.borderWidth = width;
        return this;
    }

    public TableStyle cellPadding(float padding) {
        if (padding < 0) {
            throw new ConfigurationException("Cell padding cannot be negative.");
        }

        this.cellPadding = padding;
        return this;
    }

    public RgbColor getHeaderBackground() {
        return headerBackground;
    }

    public RgbColor getHeaderTextColor() {
        return headerTextColor;
    }

    public RgbColor getRowTextColor() {
        return rowTextColor;
    }

    public RgbColor getBorderColor() {
        return borderColor;
    }

    public RgbColor getAlternateRowBackground() {
        return alternateRowBackground;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public float getCellPadding() {
        return cellPadding;
    }

    public boolean hasAlternateRowBackground() {
        return alternateRowBackground != null;
    }
}