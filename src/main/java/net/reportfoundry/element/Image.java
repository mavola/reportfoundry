package net.reportfoundry.element;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.style.Alignment;

import java.nio.file.Path;

public class Image implements Element {

    private final Path path;
    private final float width;
    private final Float height;
    private final Alignment alignment;

    public Image(Path path, float width, Alignment alignment) {
        this(path, width, null, alignment);
    }

    public Image(Path path, float width, Float height, Alignment alignment) {
        if (path == null) {
            throw new ConfigurationException("Image path cannot be null.");
        }

        if (width <= 0) {
            throw new ConfigurationException("Image width must be greater than zero.");
        }

        if (height != null && height <= 0) {
            throw new ConfigurationException("Image height must be greater than zero.");
        }

        if (alignment == null) {
            throw new ConfigurationException("Image alignment cannot be null.");
        }

        this.path = path;
        this.width = width;
        this.height = height;
        this.alignment = alignment;
    }

    public Path getPath() {
        return path;
    }

    public float getWidth() {
        return width;
    }

    public Float getHeight() {
        return height;
    }

    public boolean hasExplicitHeight() {
        return height != null;
    }

    public Alignment getAlignment() {
        return alignment;
    }
}