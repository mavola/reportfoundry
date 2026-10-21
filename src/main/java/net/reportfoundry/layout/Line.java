package net.reportfoundry.layout;

import net.reportfoundry.style.RgbColor;

public record Line(float x1, float y1, float x2, float y2, float width, RgbColor color) implements LayoutItem {
}
