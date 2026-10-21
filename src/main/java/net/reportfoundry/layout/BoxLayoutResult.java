package net.reportfoundry.layout;

import java.util.List;

public record BoxLayoutResult(List<LayoutItem> items, float height) {

    public BoxLayoutResult {
        items = List.copyOf(items);
    }
}
