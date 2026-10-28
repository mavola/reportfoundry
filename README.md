# ReportFoundry

[![Build and Test](https://github.com/mavola/reportfoundry/actions/workflows/build.yml/badge.svg)](https://github.com/mavola/reportfoundry/actions/workflows/build.yml)

**A Java library for generating PDF reports using a fluent API or HTML templates.**

ReportFoundry provides two approaches to PDF generation:

- **Fluent API:** programmatic document generation using an independent layout engine and Apache PDFBox.
- **Template API:** HTML/CSS report generation powered by Thymeleaf and OpenHTMLtoPDF.

Requires Java 17 or later.

## Features

### Fluent API

- Text, headings, images, dividers and spacers
- Tables with automatic pagination and repeated headers
- Multi-column rows and styled boxes
- Page headers, footers and page numbers
- Page configuration, margins and text styling
- Export to files, `OutputStream` or `byte[]`

### HTML Templates

- Thymeleaf data binding with Java objects and collections
- HTML/CSS layouts and automatic pagination
- Repeated table headers and page counters
- CSS, images and custom TTF fonts
- Classpath and filesystem templates
- Relative resources from conventional JAR files
- Restrictive resource access policy

## Requirements

- Java 17+
- Maven 3.x for building from source

## Installation

ReportFoundry has not yet been published to Maven Central.

Clone and install the project locally:

```bash
git clone https://github.com/mavola/reportfoundry.git
cd reportfoundry
mvn clean install
```

Add the dependency to your Maven project:

```xml
<dependency>
    <groupId>net.reportfoundry</groupId>
    <artifactId>reportfoundry</artifactId>
    <version>0.1.1</version>
</dependency>
```

## Quick Start — Fluent API

Generate a PDF programmatically without writing an HTML template.

```java
import net.reportfoundry.ReportFoundry;
import net.reportfoundry.document.PageSize;

public class Main {

    public static void main(String[] args) {
        ReportFoundry.forge()
                .page(PageSize.A4)
                .margins(40)
                .header("ReportFoundry")
                .title("Sales Report")
                .text("Monthly sales summary")
                .space(15)
                .text("Generated using the ReportFoundry Fluent API.")
                .divider()
                .pageNumbers()
                .export("sales-report.pdf");
    }
}
```

### Tables

ReportFoundry supports typed rows and column definitions.

```java
import net.reportfoundry.ReportFoundry;
import net.reportfoundry.document.PageSize;

import java.util.List;

public class TableExample {

    record Product(String name, int quantity) {}

    public static void main(String[] args) {
        List<Product> products = List.of(
                new Product("Keyboard", 2),
                new Product("Monitor", 1),
                new Product("Mouse", 3)
        );

        ReportFoundry.forge()
                .page(PageSize.A4)
                .title("Products")
                .table(products, table -> table
                        .column("Product", Product::name)
                        .column("Quantity", Product::quantity))
                .pageNumbers()
                .export("products.pdf");
    }
}
```

The Fluent API handles document layout and pagination through ReportFoundry's own layout engine.

## Quick Start — HTML Templates

Create `src/main/resources/templates/invoice.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <style>
        @page {
            size: A4;
            margin: 40px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            -fs-table-paginate: paginate;
        }

        thead {
            display: table-header-group;
        }
    </style>
</head>
<body>
    <h1 th:text="${title}">Invoice</h1>

    <table>
        <thead>
            <tr>
                <th>Product</th>
                <th>Quantity</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="item : ${items}">
                <td th:text="${item.name}">Product</td>
                <td th:text="${item.quantity}">0</td>
            </tr>
        </tbody>
    </table>
</body>
</html>
```

Generate the PDF:

```java
import net.reportfoundry.ReportFoundry;

import java.util.List;

public class InvoiceExample {

    record Item(String name, int quantity) {}

    public static void main(String[] args) {
        List<Item> items = List.of(
                new Item("Keyboard", 2),
                new Item("Monitor", 1)
        );

        ReportFoundry.template("templates/invoice.html")
                .data("title", "Invoice")
                .data("items", items)
                .export("invoice.pdf");
    }
}
```

Templates can also be loaded from the filesystem:

```java
ReportFoundry.template(Path.of("reports/invoice.html"))
        .data("title", "Invoice")
        .data("items", items)
        .export("invoice.pdf");
```

For filesystem templates, import `java.nio.file.Path`.

## Architecture

ReportFoundry provides two independent PDF generation pipelines:

```text
                  ReportFoundry
                       |
             +---------+---------+
             |                   |
         Fluent API         Template API
             |                   |
          Document            Thymeleaf
             |                   |
        LayoutEngine           HTML/CSS
             |                   |
       PdfBoxRenderer       OpenHTMLtoPDF
             |                   |
             +---------+---------+
                       |
                      PDF
```

The Fluent API uses ReportFoundry's own layout engine and Apache PDFBox.

The Template API uses Thymeleaf for template processing and OpenHTMLtoPDF for rendering HTML/CSS into PDF.

## Security

ReportFoundry is designed to process **trusted HTML templates** maintained by application developers or authorized personnel.

The HTML renderer includes a restrictive resource-resolution policy:

- HTTP/HTTPS resources are blocked by default.
- Filesystem resources are restricted to the authorized template directory.
- Resources within the authorized conventional JAR template directory are supported.
- Unauthorized resource paths are rejected.

Untrusted or user-supplied Thymeleaf templates are **not supported**. Thymeleaf is not a security sandbox.

Applications should validate input data and enforce appropriate limits on report size and resource consumption.

For security considerations and vulnerability reporting, see [SECURITY.md](SECURITY.md).

## Testing

Run the complete automated test suite:

```bash
mvn clean verify
```

ReportFoundry uses **JUnit Jupiter 6.1.3** and **Apache PDFBox** for automated testing and PDF validation.

The test suite covers:

- Fluent API and PDF document generation.
- Document layout, pagination and multi-page tables.
- Page headers, footers and page numbering.
- Thymeleaf HTML templates and resource resolution.
- External CSS, images and custom TrueType fonts.
- Spring Boot executable JAR and nested JAR resource support.
- Resource access restrictions and security regression scenarios.

Tests are automatically executed through **GitHub Actions** using Java 17 and Maven.

## Project Status

ReportFoundry is in early development.

The public API may change before the first stable release. Conventional JAR resource loading has been tested; Spring Boot executable JAR packaging has not yet been validated.

## Contributing

Contributions, bug reports and suggestions are welcome through GitHub issues and pull requests.

## License

Licensed under the [Apache License, Version 2.0](LICENSE).