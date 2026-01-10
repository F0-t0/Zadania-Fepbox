package pl.fepbox.questy;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.fepbox.questy.config.ConfigManager;
import pl.fepbox.questy.model.Requirement;
import pl.fepbox.questy.storage.PlayerQuestStorage;
import pl.fepbox.questy.storage.QuestStorage;

import java.util.List;
import java.util.Map;

public final class QuestService {

    public enum CompleteStatus {
        SUCCESS,
        NOT_ACTIVE,
        REQUIREMENTS_NOT_MET,
        LIMIT_REACHED
    }

    private final ConfigManager cfg;
    private final QuestStorage quests;
    private final PlayerQuestStorage players;

    public QuestService(ConfigManager cfg, QuestStorage quests, PlayerQuestStorage players) {
        this.cfg = cfg;
        this.quests = quests;
        this.players = players;
    }

    public boolean canComplete(Player p, String questName) {
        List<Requirement> req = quests.getRequirements(questName);
        if (req.isEmpty()) return true;
        for (Requirement r : req) {
            if (!hasEnough(p, r.item(), r.amount())) return false;
        }
        return true;
    }

    public boolean canStart(Player p, String questName) {
        int limit = completionLimit(questName);
        if (limit <= 0) return true;
        int done = players.getCompletionCount(p.getUniqueId(), questName);
        return done < limit;
    }

    public CompleteStatus complete(Player p, String questName, boolean requireRequirements) {
        if (!players.isActive(p.getUniqueId(), questName)) return CompleteStatus.NOT_ACTIVE;

        int limit = completionLimit(questName);
        if (limit > 0) {
            int done = players.getCompletionCount(p.getUniqueId(), questName);
            if (done >= limit) return CompleteStatus.LIMIT_REACHED;
        }

        if (requireRequirements && !canComplete(p, questName)) return CompleteStatus.REQUIREMENTS_NOT_MET;

        if (requireRequirements) {
            List<Requirement> req = quests.getRequirements(questName);
            for (Requirement r : req) {
                removeExact(p, r.item(), r.amount());
            }
        }

        List<ItemStack> rewards = quests.getRewards(questName);
        for (ItemStack it : rewards) {
            if (it == null || it.getType().isAir() || it.getAmount() <= 0) continue;
            Map<Integer, ItemStack> left = p.getInventory().addItem(it.clone());
            if (!left.isEmpty()) {
                for (ItemStack overflow : left.values()) {
                    if (overflow == null || overflow.getType().isAir()) continue;
                    p.getWorld().dropItemNaturally(p.getLocation(), overflow);
                }
            }
        }

        players.removeActiveQuest(p.getUniqueId(), questName);
        players.incrementCompletionCount(p.getUniqueId(), questName);
        return CompleteStatus.SUCCESS;
    }

    private int completionLimit(String questName) {
        return cfg.cfg().getInt("quests." + questName + ".completion-limit", 1);
    }

    private boolean hasEnough(Player p, ItemStack template, int amount) {
        int remaining = amount;
        ItemStack[] contents = p.getInventory().getContents();
        for (ItemStack it : contents) {
            if (it == null || it.getType().isAir()) continue;
            if (!it.isSimilar(template)) continue;
            remaining -= it.getAmount();
            if (remaining <= 0) return true;
        }
        return false;
    }

    private void removeExact(Player p, ItemStack template, int amount) {
        int remaining = amount;
        if (remaining <= 0) return;

        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack it = contents[i];
            if (it == null || it.getType().isAir()) continue;
            if (!it.isSimilar(template)) continue;

            int take = Math.min(remaining, it.getAmount());
            int newAmount = it.getAmount() - take;
            remaining -= take;

            if (newAmount <= 0) contents[i] = null;
            else it.setAmount(newAmount);

            if (remaining <= 0) break;
        }
        p.getInventory().setContents(contents);
        p.updateInventory();
    }
}
