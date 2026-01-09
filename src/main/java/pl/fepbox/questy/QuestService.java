package pl.fepbox.questy;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.fepbox.questy.storage.PlayerQuestStorage;
import pl.fepbox.questy.storage.QuestStorage;

import java.util.List;
import java.util.Map;

public final class QuestService {

    private final QuestStorage quests;
    private final PlayerQuestStorage players;

    public QuestService(QuestStorage quests, PlayerQuestStorage players) {
        this.quests = quests;
        this.players = players;
    }

    public boolean canComplete(Player p, String questName) {
        List<ItemStack> req = quests.getRequirements(questName);
        if (req.isEmpty()) return true;
        for (ItemStack r : req) {
            if (!hasEnough(p, r)) return false;
        }
        return true;
    }

    public boolean complete(Player p, String questName, boolean requireRequirements) {
        if (!players.isActive(p.getUniqueId(), questName)) return false;

        if (requireRequirements && !canComplete(p, questName)) return false;

        List<ItemStack> req = quests.getRequirements(questName);
        if (requireRequirements && !req.isEmpty()) {
            for (ItemStack r : req) {
                removeExact(p, r);
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
        players.setCompleted(p.getUniqueId(), questName, true);
        return true;
    }

    private boolean hasEnough(Player p, ItemStack need) {
        int remaining = need.getAmount();
        if (remaining <= 0) return true;
        ItemStack[] contents = p.getInventory().getContents();
        for (ItemStack it : contents) {
            if (it == null || it.getType().isAir()) continue;
            if (!it.isSimilar(need)) continue;
            remaining -= it.getAmount();
            if (remaining <= 0) return true;
        }
        return false;
    }

    private void removeExact(Player p, ItemStack need) {
        int remaining = need.getAmount();
        if (remaining <= 0) return;

        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack it = contents[i];
            if (it == null || it.getType().isAir()) continue;
            if (!it.isSimilar(need)) continue;

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
