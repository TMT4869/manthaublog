package com.manthau.postservice.shared.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class SlugUtils {

    private static final Pattern NON_LATIN     = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE    = Pattern.compile("[\\s]+");
    private static final Pattern MULTIPLE_DASH = Pattern.compile("-+");

    private SlugUtils() {}

    public static String slugify(String input) {
        if (input == null || input.isBlank()) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return MULTIPLE_DASH.matcher(
                NON_LATIN.matcher(
                        WHITESPACE.matcher(normalized.toLowerCase()).replaceAll("-")
                ).replaceAll("")
        ).replaceAll("-").replaceAll("^-|-$", "");
    }
}
