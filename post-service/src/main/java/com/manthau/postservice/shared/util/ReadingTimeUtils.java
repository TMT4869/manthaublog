package com.manthau.postservice.shared.util;

public final class ReadingTimeUtils {

    private static final int WORDS_PER_MINUTE = 200;

    private ReadingTimeUtils() {}

    public static int estimate(String content) {
        if (content == null || content.isBlank()) return 0;
        long wordCount = content.trim().split("\\s+").length;
        return (int) Math.max(1, Math.ceil((double) wordCount / WORDS_PER_MINUTE));
    }
}
