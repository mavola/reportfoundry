package net.reportfoundry.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RowLayoutResult {

    private final List<LayoutItem> items = new ArrayList<>();
    private final float height;

    public RowLayoutResult(List<LayoutItem> items, float height) {
        this.items.addAll(items);
        this.height = height;
    }

    public List<LayoutItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public float getHeight() {
        return height;
    }
}
