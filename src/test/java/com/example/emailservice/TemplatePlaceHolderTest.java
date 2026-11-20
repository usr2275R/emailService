package com.example.emailservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.thymeleaf.TemplateEngine;

import org.thymeleaf.context.Context;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TemplatePlaceholderTest {

    @Autowired
    private TemplateEngine templateEngine;

    private static final Pattern VAR =
            Pattern.compile("\\$\\{\\s*([a-zA-Z_][a-zA-Z0-9_]*)");

    @Test
    void everyPlaceholderRenders() throws Exception {
        Resource[] files = new PathMatchingResourcePatternResolver()
                .getResources("classpath:templates/**/*.html");

        for (Resource file : files) {
            String url = file.getURL().toString();
            String templateName = url.substring(url.lastIndexOf("/templates/") + 11)
                    .replace(".html", "");

            String raw = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Set<String> names = new LinkedHashSet<>();
            Matcher m = VAR.matcher(raw);
            while (m.find()) names.add(m.group(1));

            Context ctx = new Context();
            names.forEach(n -> ctx.setVariable(n, "TEST_" + n));

            String html = templateEngine.process(templateName, ctx);

            for (String n : names) {
                assertTrue(html.contains("TEST_" + n),
                        templateName + ": placeholder '" + n + "' did not render");
            }
        }
    }
}