package net.reportfoundry.template;

import net.reportfoundry.exception.TemplateException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;

public class ReportTemplateEngine {

    private final TemplateEngine templateEngine;

    public ReportTemplateEngine() {
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(false);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    public String process(String template, Map<String, Object> data) {
        if (template == null) {
            throw new TemplateException("Template content cannot be null.");
        }

        try {
            Context context = new Context();
            context.setVariables(data);
            return templateEngine.process(template, context);
        } catch (Exception e) {
            throw new TemplateException("Could not process template.", e);
        }
    }
}
