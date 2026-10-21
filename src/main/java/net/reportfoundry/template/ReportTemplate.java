package net.reportfoundry.template;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.exception.RenderException;
import net.reportfoundry.template.renderer.HtmlRenderer;
import net.reportfoundry.template.renderer.OpenHtmlToPdfRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ReportTemplate {

    private final String classpathTemplate;
    private final Path fileTemplate;
    private final Map<String, Object> data = new HashMap<>();
    private final TemplateLoader templateLoader;
    private final ReportTemplateEngine templateEngine;
    private final HtmlRenderer htmlRenderer;

    public ReportTemplate(String templatePath) {
        if (templatePath == null || templatePath.isBlank()) {
            throw new ConfigurationException("Template path cannot be null or blank.");
        }

        this.classpathTemplate = templatePath;
        this.fileTemplate = null;
        this.templateLoader = new TemplateLoader();
        this.templateEngine = new ReportTemplateEngine();
        this.htmlRenderer = new OpenHtmlToPdfRenderer();
    }

    public ReportTemplate(Path templatePath) {
        if (templatePath == null) {
            throw new ConfigurationException("Template path cannot be null.");
        }

        this.classpathTemplate = null;
        this.fileTemplate = templatePath;
        this.templateLoader = new TemplateLoader();
        this.templateEngine = new ReportTemplateEngine();
        this.htmlRenderer = new OpenHtmlToPdfRenderer();
    }

    public ReportTemplate data(String name, Object value) {
        if (name == null || name.isBlank()) {
            throw new ConfigurationException("Data name cannot be null or blank.");
        }

        data.put(name, value);
        return this;
    }

    public void export(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new ConfigurationException("Output filename cannot be null or blank.");
        }

        export(Path.of(filename));
    }

    public void export(Path path) {
        if (path == null) {
            throw new ConfigurationException("Output path cannot be null.");
        }

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            export(outputStream);
        } catch (RenderException e) {
            throw e;
        } catch (IOException e) {
            throw new RenderException("Could not write PDF to: " + path, e);
        }
    }

    public void export(OutputStream outputStream) {
        if (outputStream == null) {
            throw new ConfigurationException("Output stream cannot be null.");
        }

        LoadedTemplate loadedTemplate = fileTemplate != null ? templateLoader.load(fileTemplate) : templateLoader.load(classpathTemplate);
        String html = templateEngine.process(loadedTemplate.content(), data);
        htmlRenderer.render(html, loadedTemplate.baseUri(), outputStream);
    }

    public byte[] toBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        export(outputStream);
        return outputStream.toByteArray();
    }
}