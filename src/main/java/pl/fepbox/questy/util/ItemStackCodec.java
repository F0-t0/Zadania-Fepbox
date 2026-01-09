package pl.fepbox.questy.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.Base64;

public final class ItemStackCodec {
    private ItemStackCodec() {}

    public static String encode(ItemStack item) {
        if (item == null || item.getType().isAir()) return "";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            oos.writeObject(item);
            oos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode ItemStack", e);
        }
    }

    public static ItemStack decode(String base64) {
        if (base64 == null || base64.isBlank()) return null;
        final byte[] data;
        try {
            data = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            return null;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             BukkitObjectInputStream ois = new BukkitObjectInputStream(bais)) {
            Object o = ois.readObject();
            if (o instanceof ItemStack is) return is;
            return null;
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
}
