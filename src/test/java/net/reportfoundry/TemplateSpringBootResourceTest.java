package net.reportfoundry;

import net.reportfoundry.template.renderer.TemplateResourceResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateSpringBootResourceTest {

    private static final String BASE_URI = "jar:nested:/app/demo.jar/!BOOT-INF/classes/!/templates/reports/invoice.html";
    private static final String PREFIX = "jar:nested:/app/demo.jar/!BOOT-INF/classes/!/templates/reports/";

    @Test
    void shouldResolveRelativeCssFromSpringBootJar() {
        TemplateResourceResolver resolver = new TemplateResourceResolver(BASE_URI);

        assertEquals(PREFIX + "report.css", resolver.resolveURI(BASE_URI, "report.css"));
    }

    @Test
    void shouldResolveNestedImagesFromSpringBootJar() {
        TemplateResourceResolver resolver = new TemplateResourceResolver(BASE_URI);

        assertEquals(PREFIX + "images/logo.png", resolver.resolveURI(BASE_URI, "images/logo.png"));
    }

    @Test
    void shouldResolveFontsFromSpringBootJar() {
        TemplateResourceResolver resolver = new TemplateResourceResolver(BASE_URI);

        assertEquals(PREFIX + "fonts/custom.ttf", resolver.resolveURI(BASE_URI, "fonts/custom.ttf"));
    }

    @Test
    void shouldRejectTraversalOutsideTemplateDirectory() {
        TemplateResourceResolver resolver = new TemplateResourceResolver(BASE_URI);

        assertNull(resolver.resolveURI(BASE_URI, "../secret.txt"));
        assertNull(resolver.resolveURI(BASE_URI, "../../application.properties"));
        assertNull(resolver.resolveURI(BASE_URI, "images/../../secret.txt"));
    }

    @Test
    void shouldRejectEncodedTraversal() {
        TemplateResourceResolver resolver = new TemplateResourceResolver(BASE_URI);

        assertNull(resolver.resolveURI(BASE_URI, "%2e%2e/secret.txt"));
        assertNull(resolver.resolveURI(BASE_URI, "images/%2e%2e/secret.txt"));
    }

    @Test
    void shouldRejectRemoteResources() {
        TemplateResourceResolver resolver = new TemplateResourceResolver(BASE_URI);

        assertNull(resolver.resolveURI(BASE_URI, "https://example.com/style.css"));
        assertNull(resolver.resolveURI(BASE_URI, "http://localhost:8080/private"));
        assertNull(resolver.resolveURI(BASE_URI, "//example.com/image.png"));
    }

    @Test
    void shouldRejectResourcesFromAnotherJar() {
        TemplateResourceResolver resolver = new TemplateResourceResolver(BASE_URI);

        String external = "jar:nested:/app/other.jar/!BOOT-INF/classes/!/templates/reports/secret.css";

        assertNull(resolver.resolveURI(BASE_URI, external));
    }

    @Test
    void shouldRejectResourcesOutsideTemplateDirectoryInSameJar() {
        TemplateResourceResolver resolver = new TemplateResourceResolver(BASE_URI);

        String external = "jar:nested:/app/demo.jar/!BOOT-INF/classes/!/application.properties";

        assertNull(resolver.resolveURI(BASE_URI, external));
    }

    @Test
    void shouldRejectMalformedNestedJarBaseUri() {
        assertThrows(IllegalArgumentException.class, () -> new TemplateResourceResolver("jar:nested:/app/demo.jar!/invoice.html"));
    }
}