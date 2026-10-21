package net.reportfoundry.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LayoutPage {

    private final List<LayoutItem> items = new ArrayList<>();

    public void add(LayoutItem item) {
        items.add(item);
    }

    public List<LayoutItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
