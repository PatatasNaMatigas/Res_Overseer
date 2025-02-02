package org.g5.util;

public class StringUtil {

    public static boolean containsAny(String source, String... substrings) {
        if (source == null || substrings == null) return false;
        for (String sub : substrings) {
            if (sub != null && source.contains(sub)) {
                return true;
            }
        }
        return false;
    }
}
