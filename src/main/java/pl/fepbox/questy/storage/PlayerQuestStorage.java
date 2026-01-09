package pl.fepbox.questy.storage;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class PlayerQuestStorage {
    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration yml;

    public PlayerQuestStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
        reload();
    }

    public void reload() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create playerdata.yml", e);
            }
        }
        this.yml = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            yml.save(file);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save playerdata.yml", e);
        }
    }

    public String getActiveQuest(UUID uuid) {
        return yml.getString("players." + uuid + ".active");
    }

    public void setActiveQuest(UUID uuid, String questName) {
        String base = "players." + uuid;
        if (questName == null || questName.isBlank()) {
            yml.set(base + ".active", null);
        } else {
            yml.set(base + ".active", questName);
            yml.set(base + ".startedAt", System.currentTimeMillis());
        }
        save();
    }

    public boolean isCompleted(UUID uuid, String questName) {
        return yml.getBoolean("players." + uuid + ".completed." + questName, false);
    }

    public void setCompleted(UUID uuid, String questName, boolean value) {
        yml.set("players." + uuid + ".completed." + questName, value);
        save();
    }
}
