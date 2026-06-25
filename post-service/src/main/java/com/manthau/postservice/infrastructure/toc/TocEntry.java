package com.manthau.postservice.infrastructure.toc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TocEntry {
    private final String id;
    private final int level;
    private final String title;
    private final List<TocEntry> children = new ArrayList<>();
}
