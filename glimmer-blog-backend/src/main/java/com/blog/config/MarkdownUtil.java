package com.blog.config;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.springframework.stereotype.Component;

@Component
public class MarkdownUtil {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownUtil() {
        this.parser = Parser.builder().build();
        this.renderer = HtmlRenderer.builder().build();
    }

    public String renderToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
