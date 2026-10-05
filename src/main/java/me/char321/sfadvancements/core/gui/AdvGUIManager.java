package me.char321.sfadvancements.core.gui;

import me.char321.sfadvancements.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AdvGUIManager implements Listener {
    // click events run on each player's region thread under Folia
    private final Map<UUID, OpenGUI> guis = new ConcurrentHashMap<>();

    public void displayGUI(Player p) {
        OpenGUI gui = getByPlayer(p);
        p.openInventory(gui.getInventory());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        final Player player = (Player) e.getWhoClicked();
        OpenGUI openGUI = getByPlayer(player);
        if (e.getInventory().equals(openGUI.getInventory())) {
            e.setCancelled(true);
            int rawSlot = e.getRawSlot();
            // the GUI state (and the inventory) belongs to the player's region
            Utils.runAtEntity(player, () -> openGUI.click(player, rawSlot));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        guis.remove(e.getPlayer().getUniqueId());
    }

    public OpenGUI getByPlayer(Player p) {
        UUID uuid = p.getUniqueId();
        guis.computeIfAbsent(uuid, OpenGUI::new);
        return guis.get(uuid);
    }

    /**
     * finds out whether a player has the advancements gui open
     *
     * @param p the player
     * @return whether the player has the advancements gui open
     */
    public boolean isOpen(Player p) {
        OpenGUI openGUI = getByPlayer(p);
        return p.getOpenInventory().getTopInventory().equals(openGUI.getInventory());
    }

    public void clear() {
        guis.clear();
    }
}
