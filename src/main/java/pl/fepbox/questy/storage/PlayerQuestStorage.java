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

    public Set<String> getActiveQuests(UUID uuid) {
        String base = "players." + uuid + ".active";
        Object raw = yml.get(base);
        if (raw instanceof String s) {
            if (s.isBlank()) return Set.of();
            Set<String> out = new LinkedHashSet<>();
            out.add(s);
            yml.set(base, new ArrayList<>(out));
            save();
            return out;
        }
        List<String> list = yml.getStringList(base);
        if (list == null || list.isEmpty()) return Set.of();
        Set<String> out = new LinkedHashSet<>();
        for (String q : list) {
            if (q != null && !q.isBlank()) out.add(q);
        }
        return out;
    }

    public boolean addActiveQuest(UUID uuid, String questName) {
        if (questName == null || questName.isBlank()) return false;
        String base = "players." + uuid + ".active";
        Set<String> set = new LinkedHashSet<>(getActiveQuests(uuid));
        boolean added = set.add(questName);
        yml.set(base, new ArrayList<>(set));
        yml.set("players." + uuid + ".startedAt", System.currentTimeMillis());
        save();
        return added;
    }

    public boolean removeActiveQuest(UUID uuid, String questName) {
        if (questName == null || questName.isBlank()) return false;
        String base = "players." + uuid + ".active";
        Set<String> set = new LinkedHashSet<>(getActiveQuests(uuid));
        boolean removed = set.removeIf(q -> q.equalsIgnoreCase(questName));
        yml.set(base, new ArrayList<>(set));
        save();
        return removed;
    }

    public boolean isActive(UUID uuid, String questName) {
        for (String q : getActiveQuests(uuid)) {
            if (q.equalsIgnoreCase(questName)) return true;
        }
        return false;
    }

    public boolean isCompleted(UUID uuid, String questName) {
        return yml.getBoolean("players." + uuid + ".completed." + questName, false);
    }

    public void setCompleted(UUID uuid, String questName, boolean value) {
        yml.set("players." + uuid + ".completed." + questName, value);
        save();
    }
}
