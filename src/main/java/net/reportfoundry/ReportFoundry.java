package net.reportfoundry;

import net.reportfoundry.template.ReportTemplate;

import java.nio.file.Path;

public final class ReportFoundry {

    private ReportFoundry() {
    }

    public static ReportForge forge() {
        return new ReportForge();
    }

    public static ReportTemplate template(String templatePath) {
        return new ReportTemplate(templatePath);
    }

    public static ReportTemplate template(Path templatePath) {
        return new ReportTemplate(templatePath);
    }
}