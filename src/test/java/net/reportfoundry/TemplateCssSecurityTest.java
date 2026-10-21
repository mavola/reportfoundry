package net.reportfoundry;

import com.sun.net.httpserver.HttpServer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TemplateCssSecurityTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldRejectRemoteCssImport() throws IOException {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = createServer("/external.css", requests);
        server.start();

        try {
            int port = server.getAddress().getPort();
            Path template = tempDir.resolve("report.html");

            Files.writeString(template, """
                    <html>
                    <head>
                        <style>
                            @import url('http://127.0.0.1:%d/external.css');
                        </style>
                    </head>
                    <body>
                        <h1>Protected Report</h1>
                    </body>
                    </html>
                    """.formatted(port));

            assertValidPdf(ReportFoundry.template(template).toBytes());
            assertEquals(0, requests.get(), "Remote CSS imports must not trigger HTTP requests.");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldRejectRemoteFont() throws IOException {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = createServer("/external.ttf", requests);
        server.start();

        try {
            int port = server.getAddress().getPort();
            Path template = tempDir.resolve("report.html");

            Files.writeString(template, """
                    <html>
                    <head>
                        <style>
                            @font-face {
                                font-family: 'RemoteFont';
                                src: url('http://127.0.0.1:%d/external.ttf');
                            }
                            body { font-family: 'RemoteFont'; }
                        </style>
                    </head>
                    <body>
                        <p>Font security test</p>
                    </body>
                    </html>
                    """.formatted(port));

            assertValidPdf(ReportFoundry.template(template).toBytes());
            assertEquals(0, requests.get(), "Remote fonts must not trigger HTTP requests.");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldRejectCssImportOutsideTemplateDirectory() throws IOException {
        Path templates = Files.createDirectory(tempDir.resolve("templates"));
        Files.writeString(tempDir.resolve("external.css"), ".second { page-break-before: always; }");

        Files.writeString(templates.resolve("report.css"), """
                @import url('../external.css');
                """);

        Path template = createTemplate(templates);

        assertEquals(1, countPages(ReportFoundry.template(template).toBytes()),
                "CSS outside the template directory must not affect the PDF.");
    }

    @Test
    void shouldAllowCssImportInsideTemplateDirectory() throws IOException {
        Path templates = Files.createDirectory(tempDir.resolve("templates"));
        Path styles = Files.createDirectory(templates.resolve("styles"));

        Files.writeString(templates.resolve("report.css"), """
                @import url('styles/layout.css');
                """);

        Files.writeString(styles.resolve("layout.css"), """
                .second { page-break-before: always; }
                """);

        Path template = createTemplate(templates);

        assertEquals(2, countPages(ReportFoundry.template(template).toBytes()),
                "CSS imported from the allowed directory must be applied.");
    }

    private Path createTemplate(Path directory) throws IOException {
        Path template = directory.resolve("report.html");

        Files.writeString(template, """
                <html>
                <head>
                    <link rel="stylesheet" href="report.css"/>
                </head>
                <body>
                    <div>First page</div>
                    <div class="second">Second page</div>
                </body>
                </html>
                """);

        return template;
    }

    private HttpServer createServer(String path, AtomicInteger requests) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

        server.createContext(path, exchange -> {
            requests.incrementAndGet();
            byte[] response = "blocked".getBytes(StandardCharsets.UTF_8);

            try {
                exchange.sendResponseHeaders(200, response.length);

                try (var output = exchange.getResponseBody()) {
                    output.write(response);
                }
            } finally {
                exchange.close();
            }
        });

        return server;
    }

    private void assertValidPdf(byte[] pdf) throws IOException {
        assertTrue(pdf.length > 0);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(1, document.getNumberOfPages());
        }
    }

    private int countPages(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return document.getNumberOfPages();
        }
    }
}