package pl.fepbox.questy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import pl.fepbox.questy.commands.QuestCommand;
import pl.fepbox.questy.config.ConfigManager;
import pl.fepbox.questy.storage.PlayerQuestStorage;
import pl.fepbox.questy.storage.QuestStorage;
import pl.fepbox.questy.util.Color;

import java.util.List;

public final class FepboxQuestyPlugin extends JavaPlugin {

    private ConfigManager cfg;
    private QuestStorage quests;
    private PlayerQuestStorage players;
    private QuestService service;

    private BukkitTask checkTask;

    @Override
    public void onEnable() {
        cfg = new ConfigManager(this);
        quests = new QuestStorage(this);
        players = new PlayerQuestStorage(this);
        service = new QuestService(cfg, quests, players);

        QuestCommand cmd = new QuestCommand(this, cfg, quests, players, service);
        var pluginCommand = getCommand("quest");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(cmd);
            pluginCommand.setTabCompleter(cmd);
        }

        startChecker();
    }

    @Override
    public void onDisable() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
    }

    private void startChecker() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }

        int seconds = Math.max(1, cfg.cfg().getInt("completion.check-every-seconds", 10));
        long periodTicks = seconds * 20L;

        checkTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (cfg.cfg().getBoolean("completion.manual-only", false)) return;

            for (Player p : Bukkit.getOnlinePlayers()) {
                for (String quest : players.getActiveQuests(p.getUniqueId())) {
                    if (quest == null || quest.isBlank()) continue;
                    if (!service.canComplete(p, quest)) continue;

                    QuestService.CompleteStatus st = service.complete(p, quest, true);
                    if (st == QuestService.CompleteStatus.SUCCESS) {
                        sendCompletedMessages(p, quest);
                    }
                }
            }
        }, periodTicks, periodTicks);
    }

    private void sendCompletedMessages(Player p, String quest) {
        String prefix = cfg.msg("prefix");
        String base = cfg.msg("completed").replace("%prefix%", prefix).replace("%quest%", quest);
        if (base != null && !base.isBlank()) {
            p.sendMessage(Color.c(base));
        }

        List<String> lines = cfg.cfg().getStringList("messages.completed-lines");
        if (lines == null || lines.isEmpty()) return;

        for (String l : lines) {
            if (l == null || l.isBlank()) continue;
            String x = l.replace("%prefix%", prefix).replace("%quest%", quest);
            p.sendMessage(Color.c(x));
        }
    }
}
