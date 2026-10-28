# Changelog

All notable changes to ReportFoundry will be documented in this file.

## [Unreleased]

### Changed
- Updated OpenHTMLtoPDF PDFBox integration from `1.1.85` to `1.1.100`.
- Updated JUnit Jupiter from `5.12.2` to `6.1.3`.
- Updated Maven Surefire Plugin from `3.5.3` to `3.6.1`.
- Updated GitHub Actions `actions/setup-java` from `v4` to `v6`.
- Updated GitHub Actions `actions/checkout` from `v4` to `v7`.

### Verification
- Successfully completed GitHub Actions builds after dependency updates.
- Passed 79 automated tests with no failures, errors or skipped tests on GitHub Actions.
- Verified PDF generation using ReportFoundry `0.1.1` in a Spring Boot executable JAR.
- Confirmed correct loading of Thymeleaf templates, external CSS, PNG images and custom TrueType fonts.

## [0.1.1] - 2026-10-23

### Fixed
- Fixed HTML template resource resolution in Spring Boot executable JARs.
- Added support for Spring Boot nested JAR resource URIs.
- Fixed loading of relative CSS, images and custom TTF fonts from Spring Boot executable JARs.

### Tests
- Added regression tests for Spring Boot nested JAR URI resolution.
- Verified HTML template rendering with external CSS, PNG images and embedded TTF fonts in a Spring Boot 4.1.1 executable JAR.

## [0.1.0] - 2026-10-21

### Added
- Fluent API for programmatic PDF report generation.
- Independent document layout and pagination engine.
- Text, headings, images, dividers, rows, boxes and tables.
- Multi-page tables with repeated headers.
- Page headers, footers and page numbering.
- PDF export to files, output streams and byte arrays.
- HTML templates powered by Thymeleaf.
- Support for Java objects, collections and template expressions.
- HTML-to-PDF rendering with OpenHTMLtoPDF.
- CSS-based pagination, running headers and page counters.
- Templates loaded from classpath or filesystem.
- Relative CSS, images and custom fonts.
- Resource loading from conventional JAR files.
- Typed exception hierarchy.
- Automated JUnit 5 tests.
- Restrictive resource access policy for HTML templates.
- Protection against unauthorized local and remote resource loading.
- Security tests covering path traversal, JAR resources, CSS imports and remote fonts.
- Security policy and Dependabot configuration.