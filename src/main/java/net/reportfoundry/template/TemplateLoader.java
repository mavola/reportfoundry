package net.reportfoundry.template;

import net.reportfoundry.exception.ConfigurationException;
import net.reportfoundry.exception.TemplateException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TemplateLoader {

    public LoadedTemplate load(String templatePath) {
        if (templatePath == null || templatePath.isBlank()) {
            throw new ConfigurationException("Template path cannot be null or blank.");
        }

        String normalizedPath = templatePath.startsWith("/") ? templatePath.substring(1) : templatePath;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (classLoader == null) {
            classLoader = TemplateLoader.class.getClassLoader();
        }

        URL resource = classLoader.getResource(normalizedPath);

        if (resource == null) {
            throw new TemplateException("Template not found in classpath: " + templatePath);
        }

        try (InputStream inputStream = resource.openStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return new LoadedTemplate(content, resource.toExternalForm());
        } catch (IOException e) {
            throw new TemplateException("Could not load template from classpath: " + templatePath, e);
        }
    }

    public LoadedTemplate load(Path templatePath) {
        if (templatePath == null) {
            throw new ConfigurationException("Template path cannot be null.");
        }

        Path absolutePath = templatePath.toAbsolutePath().normalize();

        if (!Files.isRegularFile(absolutePath)) {
            throw new TemplateException("Template file not found: " + absolutePath);
        }

        try {
            String content = Files.readString(absolutePath, StandardCharsets.UTF_8);
            String baseUri = absolutePath.getParent().toUri().toString();
            return new LoadedTemplate(content, baseUri);
        } catch (IOException e) {
            throw new TemplateException("Could not load template file: " + absolutePath, e);
        }
    }
}