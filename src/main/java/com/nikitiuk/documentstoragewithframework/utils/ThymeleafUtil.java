package com.nikitiuk.documentstoragewithframework.utils;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.HashSet;
import java.util.Set;

public class ThymeleafUtil {

    private ClassLoaderTemplateResolver resolver;
    private TemplateEngine engine;

    public ThymeleafUtil() {
        this.resolver = new ClassLoaderTemplateResolver();

        resolver.setCacheable(false);
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode("HTML");
        resolver.setOrder(1);
        resolver.setCheckExistence(true);

        this.engine = new TemplateEngine();
        Set<ITemplateResolver> templateResolvers = new HashSet<>();
        templateResolvers.add(resolver);
        engine.setTemplateResolvers(templateResolvers);
    }

    public ClassLoaderTemplateResolver getResolver() {
        return resolver;
    }

    public TemplateEngine getEngine() {
        return engine;
    }
}
