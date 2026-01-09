package pl.fepbox.questy.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigManager {
    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public FileConfiguration cfg() {
        return plugin.getConfig();
    }

    public String msg(String path) {
        return cfg().getString("messages." + path, "");
    }

    public String perm(String path) {
        return cfg().getString("permissions." + path, "");
    }

    public boolean bool(String path) {
        return cfg().getBoolean(path);
    }
}
