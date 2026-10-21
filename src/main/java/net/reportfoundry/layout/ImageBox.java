package net.reportfoundry.layout;

import java.nio.file.Path;

public record ImageBox(Path path, float x, float y, float width, float height) implements LayoutItem {
}
