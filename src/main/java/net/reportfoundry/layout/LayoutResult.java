package net.reportfoundry.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LayoutResult {

    private final List<LayoutPage> pages = new ArrayList<>();

    public void addPage(LayoutPage page) {
        pages.add(page);
    }

    public List<LayoutPage> getPages() {
        return Collections.unmodifiableList(pages);
    }
}
