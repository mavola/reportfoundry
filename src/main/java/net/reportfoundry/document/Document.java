package net.reportfoundry.document;

import net.reportfoundry.element.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Document {

    private final List<Element> elements = new ArrayList<>();

    private PageSize pageSize = PageSize.A4;
    private Margins margins = Margins.all(50);

    private PageText header;
    private PageText footer;
    private PageText pageNumbers;

    public void add(Element element) {
        elements.add(element);
    }

    public List<Element> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public PageSize getPageSize() {
        return pageSize;
    }

    public void setPageSize(PageSize pageSize) {
        this.pageSize = pageSize;
    }

    public Margins getMargins() {
        return margins;
    }

    public void setMargins(Margins margins) {
        this.margins = margins;
    }

    public PageText getHeader() {
        return header;
    }

    public void setHeader(PageText header) {
        this.header = header;
    }

    public PageText getFooter() {
        return footer;
    }

    public void setFooter(PageText footer) {
        this.footer = footer;
    }

    public PageText getPageNumbers() {
        return pageNumbers;
    }

    public void setPageNumbers(PageText pageNumbers) {
        this.pageNumbers = pageNumbers;
    }

    public boolean hasHeader() {
        return header != null;
    }

    public boolean hasFooter() {
        return footer != null;
    }

    public boolean hasPageNumbers() {
        return pageNumbers != null;
    }
}
