package com.manthau.postservice.infrastructure.toc;

import java.util.List;
import java.util.Map;

public interface TocBuilder {
    List<Map<String, Object>> build(String htmlContent);
}
