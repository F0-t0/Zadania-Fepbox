package pl.fepbox.questy.util;

import org.bukkit.ChatColor;

public final class Color {
    private Color() {}

    public static String c(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
