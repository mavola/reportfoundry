package net.reportfoundry.style;

import net.reportfoundry.exception.ConfigurationException;
public record RgbColor(int red, int green, int blue) {

    public static final RgbColor BLACK = new RgbColor(0, 0, 0);
    public static final RgbColor WHITE = new RgbColor(255, 255, 255);
    public static final RgbColor LIGHT_GRAY = new RgbColor(235, 235, 235);
    public static final RgbColor GRAY = new RgbColor(128, 128, 128);
    public static final RgbColor DARK_GRAY = new RgbColor(70, 70, 70);

    public RgbColor {
        validate(red);
        validate(green);
        validate(blue);
    }

    private static void validate(int value) {
        if (value < 0 || value > 255) {
            throw new ConfigurationException("RGB values must be between 0 and 255.");
        }
    }
}