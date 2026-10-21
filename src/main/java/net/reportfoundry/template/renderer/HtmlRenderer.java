package net.reportfoundry.template.renderer;

import java.io.OutputStream;

public interface HtmlRenderer {

    void render(String html, String baseUri, OutputStream outputStream);

}
