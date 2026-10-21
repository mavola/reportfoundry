package net.reportfoundry.layout;

import net.reportfoundry.style.Alignment;
import net.reportfoundry.style.RgbColor;

public record TextLine(String text, float x, float y, float fontSize, boolean bold, Alignment alignment, RgbColor color) implements LayoutItem {
}
