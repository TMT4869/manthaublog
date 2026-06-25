package com.manthau.postservice.infrastructure.markdown;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Component;

@Component
public class MarkdownRendererImpl implements MarkdownRenderer {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownRendererImpl() {
        MutableDataSet options = new MutableDataSet();
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
    }

    @Override
    public String render(String markdownContent) {
        if (markdownContent == null || markdownContent.isBlank()) return "";
        Node document = parser.parse(markdownContent);
        return renderer.render(document);
    }
}
