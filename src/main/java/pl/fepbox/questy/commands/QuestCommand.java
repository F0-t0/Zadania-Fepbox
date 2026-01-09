package pl.fepbox.questy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import pl.fepbox.questy.config.ConfigManager;
import pl.fepbox.questy.storage.PlayerQuestStorage;
import pl.fepbox.questy.storage.QuestStorage;
import pl.fepbox.questy.util.Color;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final ConfigManager cfg;
    private final QuestStorage quests;
    private final PlayerQuestStorage players;
    private final Map<UUID, String> selectedForEdit = new ConcurrentHashMap<>();

    public QuestCommand(JavaPlugin plugin, ConfigManager cfg, QuestStorage quests, PlayerQuestStorage players) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.quests = quests;
        this.players = players;
    }

    private String prefix() {
        return cfg.msg("prefix");
    }

    private void send(CommandSender s, String raw) {
        if (raw == null) raw = "";
        raw = raw.replace("%prefix%", prefix());
        s.sendMessage(Color.c(raw));
    }

    private void send(CommandSender s, List<String> lines, String quest) {
        if (lines == null || lines.isEmpty()) return;
        for (String l : lines) {
            if (l == null) continue;
            String x = l.replace("%prefix%", prefix()).replace("%quest%", quest == null ? "" : quest);
            s.sendMessage(Color.c(x));
        }
    }

    private boolean has(CommandSender s, String permPath) {
        String node = cfg.perm(permPath);
        return node != null && !node.isBlank() && s.hasPermission(node);
    }

    private boolean isPlayerCompleteAllowed(CommandSender s) {
        if (!(s instanceof Player)) return false;
        if (!cfg.cfg().getBoolean("permissions.player-complete-enabled", false)) return false;
        String node = cfg.perm("player-complete");
        return node != null && !node.isBlank() && s.hasPermission(node);
    }

    private boolean isAdmin(CommandSender s) {
        return has(s, "admin");
    }

    private boolean isInfo(CommandSender s) {
        return has(s, "info");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            send(sender, "%prefix%&7Uzycie: &f/quest info <nazwa>");
            if (isAdmin(sender)) {
                send(sender, "%prefix%&7Admin: &f/quest create <nazwa>&7, &f/quest start <nazwa>&7, &f/quest reward <set|add|remove> ...&7, &f/quest complete <nazwa>&7, &f/quest reload");
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("reload")) {
            if (!isAdmin(sender)) {
                send(sender, cfg.msg("no-permission"));
                return true;
            }
            cfg.reload();
            quests.reload();
            players.reload();
            send(sender, cfg.msg("reload"));
            return true;
        }

        if (sub.equals("info")) {
            if (!isInfo(sender)) {
                send(sender, cfg.msg("no-permission"));
                return true;
            }
            if (args.length < 2) {
                send(sender, "%prefix%&cUzycie: /quest info <nazwa>");
                return true;
            }
            String quest = args[1];
            if (!questExistsInConfig(quest)) {
                send(sender, cfg.msg("not-found").replace("%quest%", quest));
                return true;
            }
            List<String> lines = cfg.cfg().getStringList("quests." + quest + ".message");
            send(sender, lines, quest);
            return true;
        }

        if (sub.equals("create")) {
            if (!isAdmin(sender)) {
                send(sender, cfg.msg("no-permission"));
                return true;
            }
            if (args.length < 2) {
                send(sender, "%prefix%&cUzycie: /quest create <nazwa>");
                return true;
            }
            String quest = args[1];
            ensureQuestInConfig(quest);
            plugin.saveConfig();
            quests.create(quest);
            if (sender instanceof Player p) {
                selectedForEdit.put(p.getUniqueId(), quest);
                send(sender, cfg.msg("editing-selected").replace("%quest%", quest));
            }
            send(sender, cfg.msg("created").replace("%quest%", quest));
            return true;
        }

        if (sub.equals("start")) {
            if (!isAdmin(sender)) {
                send(sender, cfg.msg("no-permission"));
                return true;
            }
            if (!(sender instanceof Player p)) {
                send(sender, cfg.msg("player-only"));
                return true;
            }
            if (args.length < 2) {
                send(sender, "%prefix%&cUzycie: /quest start <nazwa>");
                return true;
            }
            String quest = args[1];
            if (!questExistsInConfig(quest)) {
                send(sender, cfg.msg("not-found").replace("%quest%", quest));
                return true;
            }
            String active = players.getActiveQuest(p.getUniqueId());
            if (active != null && !active.isBlank()) {
                send(sender, cfg.msg("already-active").replace("%quest%", active));
                return true;
            }
            players.setActiveQuest(p.getUniqueId(), quest);
            send(sender, cfg.msg("started").replace("%quest%", quest));
            List<String> lines = cfg.cfg().getStringList("quests." + quest + ".message");
            send(sender, lines, quest);
            return true;
        }

        if (sub.equals("reward")) {
            if (!isAdmin(sender)) {
                send(sender, cfg.msg("no-permission"));
                return true;
            }
            if (!(sender instanceof Player p)) {
                send(sender, cfg.msg("player-only"));
                return true;
            }
            if (args.length < 2) {
                send(sender, "%prefix%&cUzycie: /quest reward <set|add|remove> ...");
                return true;
            }

            String mode = args[1].toLowerCase(Locale.ROOT);

            String quest;
            if (mode.equals("add")) {
                quest = args.length >= 3 ? args[2] : selectedForEdit.get(p.getUniqueId());
                if (quest == null || quest.isBlank()) {
                    send(sender, cfg.msg("editing-missing"));
                    return true;
                }
                if (!questExistsInConfig(quest)) {
                    send(sender, cfg.msg("not-found").replace("%quest%", quest));
                    return true;
                }
                ItemStack hand = p.getInventory().getItemInMainHand();
                if (hand == null || hand.getType().isAir()) {
                    send(sender, cfg.msg("reward-empty-hand"));
                    return true;
                }
                quests.create(quest);
                int n = quests.addReward(quest, hand.clone());
                send(sender, cfg.msg("reward-added").replace("%quest%", quest).replace("%number%", String.valueOf(n)));
                return true;
            }

            if (args.length < 3) {
                send(sender, "%prefix%&cUzycie: /quest reward set <numer> [nazwa]  |  /quest reward remove <numer> [nazwa]  |  /quest reward add [nazwa]");
                return true;
            }

            int number;
            try {
                number = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                send(sender, "%prefix%&cNumer musi byc liczba >= 1.");
                return true;
            }

            quest = args.length >= 4 ? args[3] : selectedForEdit.get(p.getUniqueId());
            if (quest == null || quest.isBlank()) {
                send(sender, cfg.msg("editing-missing"));
                return true;
            }
            if (!questExistsInConfig(quest)) {
                send(sender, cfg.msg("not-found").replace("%quest%", quest));
                return true;
            }

            quests.create(quest);

            if (mode.equals("remove")) {
                boolean ok = quests.removeReward(quest, number);
                if (!ok) {
                    send(sender, "%prefix%&cNie ma nagrody o numerze &f" + number + "&c.");
                    return true;
                }
                send(sender, cfg.msg("reward-removed").replace("%quest%", quest).replace("%number%", String.valueOf(number)));
                return true;
            }

            if (mode.equals("set")) {
                ItemStack hand = p.getInventory().getItemInMainHand();
                if (hand == null || hand.getType().isAir()) {
                    send(sender, cfg.msg("reward-empty-hand"));
                    return true;
                }
                quests.setReward(quest, number, hand.clone());
                send(sender, cfg.msg("reward-set").replace("%quest%", quest).replace("%number%", String.valueOf(number)));
                return true;
            }

            send(sender, "%prefix%&cUzycie: /quest reward set <numer> [nazwa]  |  /quest reward remove <numer> [nazwa]  |  /quest reward add [nazwa]");
            return true;
        }

        if (sub.equals("complete")) {
            boolean allowed = isAdmin(sender) || isPlayerCompleteAllowed(sender);
            if (!allowed) {
                send(sender, cfg.msg("no-permission"));
                return true;
            }
            if (!(sender instanceof Player p)) {
                send(sender, cfg.msg("player-only"));
                return true;
            }
            if (args.length < 2) {
                send(sender, "%prefix%&cUzycie: /quest complete <nazwa>");
                return true;
            }
            String quest = args[1];
            if (!questExistsInConfig(quest)) {
                send(sender, cfg.msg("not-found").replace("%quest%", quest));
                return true;
            }
            String active = players.getActiveQuest(p.getUniqueId());
            if (active == null || active.isBlank() || !active.equalsIgnoreCase(quest)) {
                send(sender, cfg.msg("not-active"));
                return true;
            }

            List<ItemStack> rewards = quests.getRewards(quest);
            for (ItemStack it : rewards) {
                if (it == null || it.getType().isAir()) continue;
                Map<Integer, ItemStack> left = p.getInventory().addItem(it);
                if (!left.isEmpty()) {
                    for (ItemStack overflow : left.values()) {
                        if (overflow == null || overflow.getType().isAir()) continue;
                        p.getWorld().dropItemNaturally(p.getLocation(), overflow);
                    }
                }
            }
            players.setActiveQuest(p.getUniqueId(), null);
            players.setCompleted(p.getUniqueId(), quest, true);

            send(sender, cfg.msg("completed").replace("%quest%", quest));
            return true;
        }

        send(sender, "%prefix%&cNieznana subkomenda.");
        return true;
    }

    private boolean questExistsInConfig(String quest) {
        if (quest == null || quest.isBlank()) return false;
        FileConfiguration c = cfg.cfg();
        return c.isConfigurationSection("quests." + quest);
    }

    private void ensureQuestInConfig(String quest) {
        if (quest == null || quest.isBlank()) return;
        FileConfiguration c = cfg.cfg();
        String base = "quests." + quest;
        if (c.isConfigurationSection(base)) return;
        c.createSection(base);
        c.set(base + ".enabled", true);
        c.set(base + ".message", List.of("&eWymagania zadania:", "&7- (uzupelnij w config.yml)"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = isAdmin(sender) ? List.of("info", "create", "start", "reward", "complete", "reload") : List.of("info");
            return partial(args[0], subs);
        }
        String sub = args[0].toLowerCase(Locale.ROOT);

        if (args.length == 2 && sub.equals("reward") && isAdmin(sender)) {
            return partial(args[1], List.of("set", "add", "remove"));
        }

        if (args.length == 2 && (sub.equals("info") || sub.equals("start") || sub.equals("complete"))) {
            return partial(args[1], new ArrayList<>(questKeys()));
        }

        if (sub.equals("reward") && isAdmin(sender)) {
            if (args.length == 3) {
                String mode = args[1].toLowerCase(Locale.ROOT);
                if (mode.equals("add")) {
                    return partial(args[2], new ArrayList<>(questKeys()));
                }
            }
            if (args.length == 4) {
                String mode = args[1].toLowerCase(Locale.ROOT);
                if (mode.equals("set") || mode.equals("remove")) {
                    return partial(args[3], new ArrayList<>(questKeys()));
                }
            }
        }

        return List.of();
    }

    private Set<String> questKeys() {
        FileConfiguration c = cfg.cfg();
        if (c.getConfigurationSection("quests") == null) return Set.of();
        return c.getConfigurationSection("quests").getKeys(false);
    }

    private List<String> partial(String token, List<String> options) {
        if (token == null) token = "";
        String t = token.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o == null) continue;
            if (o.toLowerCase(Locale.ROOT).startsWith(t)) out.add(o);
        }
        Collections.sort(out);
        return out;
    }
}
