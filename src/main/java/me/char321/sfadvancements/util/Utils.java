package me.char321.sfadvancements.util;

import me.char321.sfadvancements.SFAdvancements;
import me.char321.sfadvancements.api.Advancement;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.guizhanss.guizhanlib.minecraft.utils.compatibility.EnchantmentX;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class Utils {
    private Utils() {}

    private static volatile Boolean folia;

    /**
     * one-time Folia detection, cached for the lifetime of the JVM
     */
    public static boolean isFolia() {
        if (folia == null) {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                folia = true;
            } catch (ClassNotFoundException e) {
                folia = false;
            }
        }
        return folia;
    }

    public static ItemStack makeShiny(ItemStack item) {
        item = item.clone();
        ItemMeta im = item.getItemMeta();
        //noinspection DataFlowIssue
        im.addEnchant(EnchantmentX.UNBREAKING, 1, false);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(im);
        return item;
    }

    public static void makeShiny(ItemMeta im) {
        im.addEnchant(EnchantmentX.UNBREAKING, 1, false);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    public static boolean keyIsSFA(NamespacedKey key) {
        return key.getNamespace().equals(SFAdvancements.instance().getName().toLowerCase(Locale.ROOT));
    }

    public static NamespacedKey keyOf(String value) {
        return new NamespacedKey(SFAdvancements.instance(), value);
    }

    public static Advancement fromKey(String value) {
        return SFAdvancements.getRegistry().getAdvancement(keyOf(value));
    }

    public static Advancement fromKey(NamespacedKey value) {
        return SFAdvancements.getRegistry().getAdvancement(value);
    }

    public static boolean isValidAdvancement(NamespacedKey key) {
        return SFAdvancements.getRegistry().getAdvancements().containsKey(key);
    }

    public static void listen(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, SFAdvancements.instance());
    }

    public static Map<ItemStack, Integer> getContents(Inventory inv) {
        Map<ItemStack, Integer> contents = new HashMap<>();
        for (ItemStack item : inv) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            ItemStack clone = item.clone();
            clone.setAmount(1);
            contents.merge(clone, item.getAmount(), Integer::sum);
        }
        return contents;
    }

    public static TranslatableComponent getItemName(ItemStack item) {
        return getItemName(item.getType());
    }

    public static TranslatableComponent getItemName(Material type) {
        if (type.isBlock()) {
            return new TranslatableComponent("block.minecraft."+type.getKey().getKey());
        } else {
            return new TranslatableComponent("item.minecraft."+type.getKey().getKey());
        }
    }

    /**
     * runs the task on the global region thread
     */
    public static void runSync(Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().run(SFAdvancements.instance(), (task) -> runnable.run());
    }

    /**
     * runs the task on the global region thread after the given delay in ticks
     */
    public static void runLater(Runnable runnable, long delay) {
        Bukkit.getGlobalRegionScheduler().runDelayed(SFAdvancements.instance(), (task) -> runnable.run(), delay);
    }

    /**
     * schedules a repeating task on the global region thread; delays are in game ticks
     */
    public static void runSyncAtFixedRate(Runnable runnable, long initialDelayTicks, long periodTicks) {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(SFAdvancements.instance(),
                (task) -> runnable.run(), initialDelayTicks, periodTicks);
    }

    /**
     * runs the task in the entity's owning scheduler context (entity region thread under Folia)
     */
    public static void runAtEntity(Entity entity, Runnable runnable) {
        if (isFolia()) {
            entity.getScheduler().run(SFAdvancements.instance(), (task) -> runnable.run(), null);
        } else {
            Bukkit.getScheduler().runTask(SFAdvancements.instance(), runnable);
        }
    }

    /**
     * runs the task in the entity's owning scheduler context after the given delay
     * (Folia entity scheduling always incurs at least 1 tick of delay)
     */
    public static void runAtEntityLater(Entity entity, Runnable runnable, long delay) {
        if (delay < 1) {
            delay = 1;
        }
        if (isFolia()) {
            entity.getScheduler().runDelayed(SFAdvancements.instance(), (task) -> runnable.run(), null, delay);
        } else {
            Bukkit.getScheduler().runTaskLater(SFAdvancements.instance(), runnable, delay);
        }
    }

    /**
     * runs the task on an async thread; must not touch live region/entity state
     */
    public static void runAsync(Runnable runnable) {
        if (isFolia()) {
            Bukkit.getAsyncScheduler().runNow(SFAdvancements.instance(), (task) -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(SFAdvancements.instance(), runnable);
        }
    }

    /**
     * schedules a repeating task on an async thread; delays are in game ticks
     */
    public static void runAsyncAtFixedRate(Runnable runnable, long initialDelayTicks, long periodTicks) {
        long initialMillis = ticksToMillis(initialDelayTicks);
        long periodMillis = ticksToMillis(periodTicks);
        if (isFolia()) {
            Bukkit.getAsyncScheduler().runAtFixedRate(SFAdvancements.instance(),
                    (task) -> runnable.run(), initialMillis, periodMillis, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(SFAdvancements.instance(), runnable,
                    initialDelayTicks, periodTicks);
        }
    }

    public static long ticksToMillis(long ticks) {
        return ticks * 50L;
    }
}
