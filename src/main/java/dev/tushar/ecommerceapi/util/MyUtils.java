package dev.tushar.ecommerceapi.util;

import java.util.regex.Pattern;

public final class MyUtils {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("#[0-9A-Fa-f]{6}");

    private MyUtils() {}

    public static Long getParentIdFromPath(String path) {
        if (path == null || !path.contains("/")) {
            return null;
        }
        String[] pathParts = path.substring(0, path.length() - 1).split("/");
        return (pathParts.length > 1) ? Long.parseLong(pathParts[pathParts.length - 2]) : null;
    }

    public static boolean validateColorHex(String colorHex) {
        return colorHex != null && HEX_COLOR_PATTERN.matcher(colorHex).matches();
    }
}