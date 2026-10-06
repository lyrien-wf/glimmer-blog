package com.blog.config;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MarkdownUtil {

    private final Parser parser;
    private final HtmlRenderer renderer;
    private final Safelist safelist;

    public MarkdownUtil() {
        // flexmark 核心只实现 CommonMark：表格、删除线、任务列表、自动链接都需要显式注册扩展。
        // 并且 parser 与 renderer 必须共享同一份 options，否则语法被解析、HTML 却渲染不出来。
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(
                TablesExtension.create(),        // GFM 表格
                StrikethroughExtension.create(), // ~~删除线~~
                TaskListExtension.create(),      // - [x] 任务列表
                AutolinkExtension.create()       // 裸 URL 自动成链接
        ));

        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();

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
