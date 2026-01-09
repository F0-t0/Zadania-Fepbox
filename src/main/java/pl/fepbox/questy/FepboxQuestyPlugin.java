package pl.fepbox.questy;

import org.bukkit.plugin.java.JavaPlugin;
import pl.fepbox.questy.commands.QuestCommand;
import pl.fepbox.questy.config.ConfigManager;
import pl.fepbox.questy.storage.PlayerQuestStorage;
import pl.fepbox.questy.storage.QuestStorage;

public final class FepboxQuestyPlugin extends JavaPlugin {

    private ConfigManager cfg;
    private QuestStorage quests;
    private PlayerQuestStorage players;

    @Override
    public void onEnable() {
        cfg = new ConfigManager(this);
        quests = new QuestStorage(this);
        players = new PlayerQuestStorage(this);

        QuestCommand cmd = new QuestCommand(this, cfg, quests, players);
        var pluginCommand = getCommand("quest");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(cmd);
            pluginCommand.setTabCompleter(cmd);
        }
    }
}
