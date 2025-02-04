package org.g5.util;

public class StringUtil {

    public static boolean containsAny(String source, boolean ignoreCase, String... substrings) {
        if (source == null || substrings == null) return false;
        if (ignoreCase) {
            source = source.toLowerCase();
            for (int i = 0; i < substrings.length; i++) {
                substrings[i] = substrings[i].toLowerCase();
            }
        }
        for (String sub : substrings) {
            if (sub != null && source.contains(sub)) {
                return true;
            }
        }
        return false;
    }
}
