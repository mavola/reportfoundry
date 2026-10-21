package net.reportfoundry;

import net.reportfoundry.template.renderer.TemplateResourceResolver;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TemplateSecurityTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldAllowRelativeFileInsideTemplateDirectory() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("templates"));
        Path stylesheet = Files.writeString(directory.resolve("report.css"), "body { color: black; }");

        TemplateResourceResolver resolver = new TemplateResourceResolver(directory.toUri().toString());

        assertEquals(stylesheet.toRealPath().toUri().toString(), resolver.resolveURI(directory.toUri().toString(), "report.css"));
    }

    @Test
    void shouldAllowNestedFileInsideTemplateDirectory() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("templates"));
        Path images = Files.createDirectory(directory.resolve("images"));
        Path image = Files.write(images.resolve("logo.png"), new byte[]{1, 2, 3});

        TemplateResourceResolver resolver = new TemplateResourceResolver(directory.toUri().toString());

        assertEquals(image.toRealPath().toUri().toString(), resolver.resolveURI(directory.toUri().toString(), "images/logo.png"));
    }

    @Test
    void shouldRejectHttpAndHttpsResources() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("templates"));
        TemplateResourceResolver resolver = new TemplateResourceResolver(directory.toUri().toString());

        assertNull(resolver.resolveURI(directory.toUri().toString(), "http://127.0.0.1:8080/internal"));
        assertNull(resolver.resolveURI(directory.toUri().toString(), "https://example.com/image.png"));
        assertNull(resolver.resolveURI(directory.toUri().toString(), "//example.com/image.png"));
    }

    @Test
    void shouldRejectFileTraversalOutsideTemplateDirectory() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("templates"));
        Files.writeString(tempDir.resolve("secret.txt"), "private");

        TemplateResourceResolver resolver = new TemplateResourceResolver(directory.toUri().toString());

        assertNull(resolver.resolveURI(directory.toUri().toString(), "../secret.txt"));
        assertNull(resolver.resolveURI(directory.toUri().toString(), "images/../../secret.txt"));
    }

    @Test
    void shouldRejectEncodedFileTraversal() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("templates"));
        Files.writeString(tempDir.resolve("secret.txt"), "private");

        TemplateResourceResolver resolver = new TemplateResourceResolver(directory.toUri().toString());

        assertNull(resolver.resolveURI(directory.toUri().toString(), "%2e%2e/secret.txt"));
        assertNull(resolver.resolveURI(directory.toUri().toString(), "images/%2e%2e/%2e%2e/secret.txt"));
    }

    @Test
    void shouldRejectAbsoluteFileOutsideTemplateDirectory() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("templates"));
        Path secret = Files.writeString(tempDir.resolve("secret.txt"), "private");

        TemplateResourceResolver resolver = new TemplateResourceResolver(directory.toUri().toString());

        assertNull(resolver.resolveURI(directory.toUri().toString(), secret.toUri().toString()));
    }

    @Test
    void shouldRejectSymlinkOutsideTemplateDirectory() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("templates"));
        Path secret = Files.writeString(tempDir.resolve("secret.txt"), "private");
        Path link = directory.resolve("linked-secret.txt");

        try {
            Files.createSymbolicLink(link, secret);
        } catch (UnsupportedOperationException | IOException | SecurityException e) {
            Assumptions.abort("Symbolic links are unavailable in this environment.");
        }

        TemplateResourceResolver resolver = new TemplateResourceResolver(directory.toUri().toString());

        assertNull(resolver.resolveURI(directory.toUri().toString(), "linked-secret.txt"));
    }

    @Test
    void shouldAllowResourceInsideSameJar() throws IOException {
        Path jar = Files.createFile(tempDir.resolve("templates.jar"));
        String baseUri = "jar:" + jar.toUri() + "!/templates/report.html";

        TemplateResourceResolver resolver = new TemplateResourceResolver(baseUri);

        String resolved = resolver.resolveURI(baseUri, "report.css");

        assertNotNull(resolved);
        assertTrue(resolved.endsWith("!/templates/report.css"));
    }

    @Test
    void shouldRejectJarTraversalOutsideTemplateDirectory() throws IOException {
        Path jar = Files.createFile(tempDir.resolve("templates.jar"));
        String baseUri = "jar:" + jar.toUri() + "!/templates/report.html";

        TemplateResourceResolver resolver = new TemplateResourceResolver(baseUri);

        assertNull(resolver.resolveURI(baseUri, "../secret.txt"));
        assertNull(resolver.resolveURI(baseUri, "../../secret.txt"));
    }

    @Test
    void shouldRejectEncodedJarTraversal() throws IOException {
        Path jar = Files.createFile(tempDir.resolve("templates.jar"));
        String baseUri = "jar:" + jar.toUri() + "!/templates/report.html";

        TemplateResourceResolver resolver = new TemplateResourceResolver(baseUri);

        assertNull(resolver.resolveURI(baseUri, "%2e%2e/secret.txt"));
        assertNull(resolver.resolveURI(baseUri, "%2e%2e%2fsecret.txt"));
    }

    @Test
    void shouldRejectResourcesFromAnotherJar() throws IOException {
        Path allowedJar = Files.createFile(tempDir.resolve("allowed.jar"));
        Path otherJar = Files.createFile(tempDir.resolve("other.jar"));
        String baseUri = "jar:" + allowedJar.toUri() + "!/templates/report.html";
        String externalUri = "jar:" + otherJar.toUri() + "!/templates/secret.css";

        TemplateResourceResolver resolver = new TemplateResourceResolver(baseUri);

        assertNull(resolver.resolveURI(baseUri, externalUri));
    }

    @Test
    void shouldRejectUnsupportedSchemes() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("templates"));
        TemplateResourceResolver resolver = new TemplateResourceResolver(directory.toUri().toString());

        assertNull(resolver.resolveURI(directory.toUri().toString(), "ftp://example.com/file.css"));
        assertNull(resolver.resolveURI(directory.toUri().toString(), "data:text/plain,secret"));
        assertNull(resolver.resolveURI(directory.toUri().toString(), "jar:file:///tmp/other.jar!/secret.css"));
    }

    @Test
    void shouldRejectWindowsStyleTraversal() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("templates"));
        TemplateResourceResolver resolver = new TemplateResourceResolver(directory.toUri().toString());

        assertNull(resolver.resolveURI(directory.toUri().toString(), "..\\secret.txt"));
        assertNull(resolver.resolveURI(directory.toUri().toString(), "images\\..\\secret.txt"));
    }
}
