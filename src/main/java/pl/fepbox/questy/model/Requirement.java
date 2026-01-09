package pl.fepbox.questy.model;

import org.bukkit.inventory.ItemStack;

public record Requirement(ItemStack item, int amount) {
    public Requirement {
        if (item == null) throw new IllegalArgumentException("item");
        if (amount < 1) throw new IllegalArgumentException("amount");
    }
}
