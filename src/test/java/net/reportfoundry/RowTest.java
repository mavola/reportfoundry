package net.reportfoundry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RowTest {

    @Test
    void shouldGenerateMultipleColumns() {
        assertDoesNotThrow(() -> ReportFoundry.forge()
                .row(row -> row
                        .column(50, column -> column
                                .text("Customer information")
                                .text("ACME Corporation")
                        )
                        .column(50, column -> column
                                .text("Invoice information")
                                .text("INV-2026-001")
                        )
                ).toBytes()
        );
    }

    @Test
    void rowShouldSupportWrappedText() {
        assertDoesNotThrow(() -> ReportFoundry.forge()
                .row(row -> row
                        .column(50, column -> column
                                .text("This is a considerably longer text that should wrap inside the first column.")
                        )
                        .column(50, column -> column
                                .text("Second column")
                        )
                ).toBytes()
        );
    }
}
