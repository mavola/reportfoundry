package net.reportfoundry.template.renderer;

import com.openhtmltopdf.extend.FSUriResolver;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TemplateResourceResolver implements FSUriResolver {

    private final Path allowedDirectory;
    private final String jarPrefix;
    private final String jarDirectory;

    public TemplateResourceResolver(String baseUri) {
        try {
            URI templateBase = URI.create(baseUri);

            if ("file".equalsIgnoreCase(templateBase.getScheme())) {
                Path basePath = Path.of(templateBase).toRealPath();
                allowedDirectory = Files.isDirectory(basePath) ? basePath : basePath.getParent();
                jarPrefix = null;
                jarDirectory = null;
            } else if ("jar".equalsIgnoreCase(templateBase.getScheme())) {
                String value = templateBase.toString();
                int separator = value.indexOf("!/");

                if (!value.startsWith("jar:file:") || separator < 0) {
                    throw new IllegalArgumentException("Unsupported JAR base URI.");
                }

                Path jarFile = Path.of(URI.create(value.substring(4, separator))).toRealPath();
                jarPrefix = "jar:" + jarFile.toUri() + "!/";

                String entry = value.substring(separator + 2);

                if (entry.contains("%") || entry.contains("\\") || entry.isBlank()) {
                    throw new IllegalArgumentException("Unsupported JAR template path.");
                }

                int lastSlash = entry.lastIndexOf('/');
                jarDirectory = lastSlash >= 0 ? entry.substring(0, lastSlash + 1) : "";
                allowedDirectory = null;
            } else {
                throw new IllegalArgumentException("Unsupported template base URI.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid template base URI: " + baseUri, e);
        }
    }

    @Override
    public String resolveURI(String baseUri, String uri) {
        if (uri == null || uri.isBlank()) {
            return null;
        }

        try {
            String value = uri.trim();

            if (value.indexOf('\\') >= 0 || value.startsWith("//")) {
                return null;
            }

            URI requested = URI.create(value);

            if (requested.getRawQuery() != null || requested.getRawFragment() != null) {
                return null;
            }

            if (allowedDirectory != null) {
                return resolveFile(requested);
            }

            return resolveJar(requested);
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveFile(URI requested) throws Exception {
        Path target;

        if (requested.isAbsolute()) {
            if (!"file".equalsIgnoreCase(requested.getScheme())) {
                return null;
            }

            target = Path.of(requested);
        } else {
            if (requested.getRawAuthority() != null || requested.getPath().startsWith("/")) {
                return null;
            }

            target = allowedDirectory.resolve(requested.getPath());
        }

        Path normalized = target.toAbsolutePath().normalize();

        if (!normalized.startsWith(allowedDirectory) || !Files.isRegularFile(normalized)) {
            return null;
        }

        Path realPath = normalized.toRealPath();

        if (!realPath.startsWith(allowedDirectory)) {
            return null;
        }

        return realPath.toUri().toString();
    }

    private String resolveJar(URI requested) {
        String path;

        if (requested.isAbsolute()) {
            String value = requested.toString();

            if (!value.startsWith(jarPrefix)) {
                return null;
            }

            path = value.substring(jarPrefix.length());
        } else {
            if (requested.getRawAuthority() != null || requested.getPath().startsWith("/")) {
                return null;
            }

            path = jarDirectory + requested.getRawPath();
        }

        if (path.contains("%") || path.contains("\\") || path.indexOf('\0') >= 0) {
            return null;
        }

        String normalized = normalizeJarEntry(path);

        if (normalized == null || !normalized.startsWith(jarDirectory)) {
            return null;
        }

        return jarPrefix + normalized;
    }

    private String normalizeJarEntry(String path) {
        List<String> segments = new ArrayList<>();

        for (String segment : path.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".")) {
                continue;
            }

            if (segment.equals("..")) {
                if (segments.isEmpty()) {
                    return null;
                }

                segments.remove(segments.size() - 1);
            } else {
                segments.add(segment);
            }
        }

        return String.join("/", segments);
    }
}