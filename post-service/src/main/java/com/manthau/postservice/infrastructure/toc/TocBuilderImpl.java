package com.manthau.postservice.infrastructure.toc;

import com.manthau.postservice.shared.util.SlugUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TocBuilderImpl implements TocBuilder {

    @Override
    public List<Map<String, Object>> build(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> currentH2 = null;

        for (Element el : doc.select("h2, h3")) {
            int level = Integer.parseInt(el.tagName().substring(1));
            String title = el.text();
            String id = SlugUtils.slugify(title);
            el.attr("id", id);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", id);
            entry.put("level", level);
            entry.put("title", title);
            entry.put("children", new ArrayList<>());

            if (level == 2) {
                result.add(entry);
                currentH2 = entry;
            } else if (level == 3 && currentH2 != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) currentH2.get("children");
                children.add(entry);
            }
        }
        return result;
    }
}
