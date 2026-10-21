package net.reportfoundry.renderer;

import net.reportfoundry.document.Document;

import java.io.OutputStream;
import java.nio.file.Path;

public interface Renderer {

    void render(Document document, Path path);

    void render(Document document, OutputStream outputStream);
}