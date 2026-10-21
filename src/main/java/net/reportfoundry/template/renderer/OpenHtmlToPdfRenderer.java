
package net.reportfoundry.template.renderer;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import net.reportfoundry.exception.RenderException;

import java.io.OutputStream;

public class OpenHtmlToPdfRenderer implements HtmlRenderer {

    @Override
    public void render(String html, String baseUri, OutputStream outputStream) {
        if (html == null) {
            throw new RenderException("HTML content cannot be null.");
        }

        if (outputStream == null) {
            throw new RenderException("Output stream cannot be null.");
        }

        try {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useUriResolver(new TemplateResourceResolver(baseUri));
            builder.withHtmlContent(html, baseUri);
            builder.toStream(outputStream);
            builder.run();
        } catch (Exception e) {
            throw new RenderException("Could not render HTML template to PDF.", e);
        }
    }
}

