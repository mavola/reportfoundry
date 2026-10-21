package net.reportfoundry.document;

public enum PageSize {

    A4(595.28f, 841.89f),
    LETTER(612f, 792f);

    private final float width;
    private final float height;

    PageSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
