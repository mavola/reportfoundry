# Security Policy

## Supported Versions

ReportFoundry is currently under active development.

| Version | Supported |
|---|-----------|
| 0.1.0 | Yes       |
| Older versions | No        |

Security support information will be updated when the first stable release is published.

## Security Model

ReportFoundry provides two approaches to PDF generation:

- A fluent Java API with its own layout and rendering engine.
- HTML templates processed using Thymeleaf and OpenHTMLtoPDF.

### Trusted Templates

ReportFoundry is designed to process trusted HTML templates maintained by application developers or other authorized personnel.

**Untrusted or user-supplied Thymeleaf templates are not supported.** Thymeleaf must not be treated as a security sandbox.

### Resource Access

The HTML rendering pipeline applies a restrictive resource-resolution policy.

- HTTP and HTTPS resources are blocked by default.
- Local resources are restricted to the authorized template directory.
- Resources within the authorized JAR template directory are supported.
- Attempts to access unauthorized files and JAR resources are rejected.

These restrictions have automated unit and integration tests, but they do not guarantee complete isolation against every possible attack.

### Resource Consumption

ReportFoundry does not currently enforce comprehensive limits on PDF size, page count, rendering time, image dimensions, or memory consumption.

Applications integrating ReportFoundry should validate input data and apply appropriate resource limits.

## Reporting a Vulnerability

Please report suspected security vulnerabilities privately through GitHub Security Advisories:

https://github.com/mavola/reportfoundry/security/advisories/new

Do not disclose exploitable vulnerabilities through public GitHub issues before a fix or mitigation has been coordinated.

Please include reproduction steps, the affected version, the expected and observed behavior, and a minimal example when possible.

## Security Testing

The project includes automated tests covering resource access restrictions, path traversal, encoded paths, JAR isolation, CSS imports, and remote resource loading.

Security tests are not a substitute for a comprehensive security audit.