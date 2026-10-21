package net.reportfoundry.document;

public record Margins(float top, float right, float bottom, float left) {

    public static Margins all(float value) {
        return new Margins(value, value, value, value);
    }
}