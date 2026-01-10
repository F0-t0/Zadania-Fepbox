package pl.fepbox.questy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import pl.fepbox.questy.commands.QuestCommand;
import pl.fepbox.questy.config.ConfigManager;
import pl.fepbox.questy.storage.PlayerQuestStorage;
import pl.fepbox.questy.storage.QuestStorage;

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
        if (checkTask != null) checkTask.cancel();
    }

    private void startChecker() {
        if (checkTask != null) checkTask.cancel();
        int seconds = Math.max(1, cfg.cfg().getInt("completion.check-every-seconds", 10));
        long periodTicks = seconds * 20L;

        checkTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            boolean manualOnly = cfg.cfg().getBoolean("completion.manual-only", false);
            if (manualOnly) return;

            for (Player p : Bukkit.getOnlinePlayers()) {
                for (String quest : players.getActiveQuests(p.getUniqueId())) {
                    if (quest == null || quest.isBlank()) continue;
                    if (service.canComplete(p, quest)) {
                        service.complete(p, quest, true);
                    }
                }
            }
        }, periodTicks, periodTicks);
    }
}
