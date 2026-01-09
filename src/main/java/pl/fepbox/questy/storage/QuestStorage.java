package pl.fepbox.questy.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import pl.fepbox.questy.util.ItemStackCodec;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class QuestStorage {
    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration yml;

    public QuestStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "quests.yml");
        reload();
    }

    public void reload() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create quests.yml", e);
            }
        }
        this.yml = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            yml.save(file);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save quests.yml", e);
        }
    }

    public Set<String> listQuests() {
        ConfigurationSection sec = yml.getConfigurationSection("quests");
        if (sec == null) return Collections.emptySet();
        Set<String> names = new HashSet<>();
        for (String key : sec.getKeys(false)) names.add(key);
        return names;
    }

    public boolean exists(String name) {
        return yml.isConfigurationSection("quests." + name);
    }

    public void create(String name) {
        String base = "quests." + name;
        if (yml.isConfigurationSection(base)) return;
        yml.createSection(base);
        yml.set(base + ".rewards", new ArrayList<String>());
        yml.set(base + ".requirements", new ArrayList<String>());
        save();
    }

    public List<ItemStack> getRewards(String name) {
        return decodeList("quests." + name + ".rewards");
    }

    public List<ItemStack> getRequirements(String name) {
        return decodeList("quests." + name + ".requirements");
    }

    private List<ItemStack> decodeList(String path) {
        List<String> encoded = yml.getStringList(path);
        if (encoded == null || encoded.isEmpty()) return List.of();
        List<ItemStack> out = new ArrayList<>(encoded.size());
        for (String s : encoded) {
            ItemStack is = ItemStackCodec.decode(s);
            if (is != null && !is.getType().isAir() && is.getAmount() > 0) out.add(is);
        }
        return out;
    }

    private void setAt(String path, int number1Based, ItemStack item) {
        if (number1Based < 1) throw new IllegalArgumentException("number must be >= 1");
        List<String> encoded = new ArrayList<>(yml.getStringList(path));
        int idx = number1Based - 1;
        while (encoded.size() <= idx) encoded.add("");
        encoded.set(idx, ItemStackCodec.encode(item));
        yml.set(path, encoded);
        save();
    }

    private int addTo(String path, ItemStack item) {
        List<String> encoded = new ArrayList<>(yml.getStringList(path));
        encoded.add(ItemStackCodec.encode(item));
        yml.set(path, encoded);
        save();
        return encoded.size();
    }

    private boolean removeAt(String path, int number1Based) {
        if (number1Based < 1) return false;
        List<String> encoded = new ArrayList<>(yml.getStringList(path));
        int idx = number1Based - 1;
        if (idx < 0 || idx >= encoded.size()) return false;
        encoded.remove(idx);
        yml.set(path, encoded);
        save();
        return true;
    }

    public void setReward(String name, int number1Based, ItemStack item) {
        setAt("quests." + name + ".rewards", number1Based, item);
    }

    public int addReward(String name, ItemStack item) {
        return addTo("quests." + name + ".rewards", item);
    }

    public boolean removeReward(String name, int number1Based) {
        return removeAt("quests." + name + ".rewards", number1Based);
    }

    public void setRequirement(String name, int number1Based, ItemStack item) {
        setAt("quests." + name + ".requirements", number1Based, item);
    }

    public int addRequirement(String name, ItemStack item) {
        return addTo("quests." + name + ".requirements", item);
    }

    public boolean removeRequirement(String name, int number1Based) {
        return removeAt("quests." + name + ".requirements", number1Based);
    }
}
