package net.reportfoundry.layout;

import net.reportfoundry.style.RgbColor;

public record FilledRectangle(float x, float y, float width, float height, RgbColor color) implements LayoutItem {
}
