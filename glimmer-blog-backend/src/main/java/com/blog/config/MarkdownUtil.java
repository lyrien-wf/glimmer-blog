package com.blog.config;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class MarkdownUtil {

    private final Parser parser;
    private final HtmlRenderer renderer;
    private final Safelist safelist;

    public MarkdownUtil() {
        this.parser = Parser.builder().build();
        this.renderer = HtmlRenderer.builder().build();

        // HTML 白名单：允许常见富文本标签，禁止 script/onclick 等危险内容
        this.safelist = Safelist.relaxed()
                .addAttributes(":all", "class", "id")      // 代码高亮、标题锚点需要 class/id
                .addTags("hr", "del", "input")             // 分割线、删除线、任务列表复选框
                .addAttributes("input", "type", "checked", "disabled")
                .addProtocols("img", "src", "http", "https", "data")
                .preserveRelativeLinks(true);              // 保留 /uploads/ 相对路径
    }

    public String renderToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        Node document = parser.parse(markdown);
        String rawHtml = renderer.render(document);
        // 净化 HTML，过滤 XSS
        return Jsoup.clean(rawHtml, "", safelist);
    }
}
