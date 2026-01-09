package pl.fepbox.questy.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import pl.fepbox.questy.model.Requirement;
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
        yml.set(base + ".requirements", new ArrayList<Map<String, Object>>());
        save();
    }

    public List<ItemStack> getRewards(String name) {
        List<String> encoded = yml.getStringList("quests." + name + ".rewards");
        if (encoded == null || encoded.isEmpty()) return List.of();
        List<ItemStack> out = new ArrayList<>(encoded.size());
        for (String s : encoded) {
            ItemStack is = ItemStackCodec.decode(s);
            if (is != null && !is.getType().isAir() && is.getAmount() > 0) out.add(is);
        }
        return out;
    }

    public List<Requirement> getRequirements(String name) {
        List<Map<?, ?>> list = yml.getMapList("quests." + name + ".requirements");
        if (list == null || list.isEmpty()) return List.of();
        List<Requirement> out = new ArrayList<>(list.size());
        for (Map<?, ?> m : list) {
            if (m == null) continue;
            Object itemRaw = m.get("item");
            Object amountRaw = m.get("amount");
            if (!(itemRaw instanceof String itemB64)) continue;
            int amount = 0;
            if (amountRaw instanceof Number n) amount = n.intValue();
            else if (amountRaw instanceof String s) {
                try { amount = Integer.parseInt(s); } catch (NumberFormatException ignored) {}
            }
            if (amount < 1) continue;
            ItemStack item = ItemStackCodec.decode(itemB64);
            if (item == null || item.getType().isAir()) continue;
            item.setAmount(1);
            out.add(new Requirement(item, amount));
        }
        return out;
    }

    public void setReward(String name, int number1Based, ItemStack item) {
        if (number1Based < 1) throw new IllegalArgumentException("number must be >= 1");
        List<String> encoded = new ArrayList<>(yml.getStringList("quests." + name + ".rewards"));
        int idx = number1Based - 1;
        while (encoded.size() <= idx) encoded.add("");
        encoded.set(idx, ItemStackCodec.encode(item));
        yml.set("quests." + name + ".rewards", encoded);
        save();
    }

    public int addReward(String name, ItemStack item) {
        List<String> encoded = new ArrayList<>(yml.getStringList("quests." + name + ".rewards"));
        encoded.add(ItemStackCodec.encode(item));
        yml.set("quests." + name + ".rewards", encoded);
        save();
        return encoded.size();
    }

    public boolean removeReward(String name, int number1Based) {
        if (number1Based < 1) return false;
        List<String> encoded = new ArrayList<>(yml.getStringList("quests." + name + ".rewards"));
        int idx = number1Based - 1;
        if (idx < 0 || idx >= encoded.size()) return false;
        encoded.remove(idx);
        yml.set("quests." + name + ".rewards", encoded);
        save();
        return true;
    }

    public void setRequirement(String name, int number1Based, ItemStack itemTemplate, int amount) {
        if (number1Based < 1) throw new IllegalArgumentException("number must be >= 1");
        if (amount < 1) throw new IllegalArgumentException("amount must be >= 1");

        List<Map<?, ?>> list = new ArrayList<>(yml.getMapList("quests." + name + ".requirements"));
        int idx = number1Based - 1;
        while (list.size() <= idx) list.add(new LinkedHashMap<>());

        Map<String, Object> map = new LinkedHashMap<>();
        ItemStack t = itemTemplate.clone();
        t.setAmount(1);
        map.put("item", ItemStackCodec.encode(t));
        map.put("amount", amount);
        list.set(idx, map);

        yml.set("quests." + name + ".requirements", list);
        save();
    }

    public int addRequirement(String name, ItemStack itemTemplate, int amount) {
        if (amount < 1) throw new IllegalArgumentException("amount must be >= 1");
        List<Map<?, ?>> list = new ArrayList<>(yml.getMapList("quests." + name + ".requirements"));

        Map<String, Object> map = new LinkedHashMap<>();
        ItemStack t = itemTemplate.clone();
        t.setAmount(1);
        map.put("item", ItemStackCodec.encode(t));
        map.put("amount", amount);
        list.add(map);

        yml.set("quests." + name + ".requirements", list);
        save();
        return list.size();
    }

    public boolean removeRequirement(String name, int number1Based) {
        if (number1Based < 1) return false;
        List<Map<?, ?>> list = new ArrayList<>(yml.getMapList("quests." + name + ".requirements"));
        int idx = number1Based - 1;
        if (idx < 0 || idx >= list.size()) return false;
        list.remove(idx);
        yml.set("quests." + name + ".requirements", list);
        save();
        return true;
    }
}
