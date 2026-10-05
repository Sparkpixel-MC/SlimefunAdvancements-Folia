package me.char321.sfadvancements.vanilla;

import me.char321.sfadvancements.SFAdvancements;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // warm the progress cache off the region thread; the first load reads a file from disk
        me.char321.sfadvancements.util.Utils.runAsync(() ->
                SFAdvancements.getAdvManager().getProgress(e.getPlayer().getUniqueId()));
        if (SFAdvancements.getMainConfig().getBoolean("use-advancements-api")){
            SFAdvancements.getVanillaHook().syncProgress(e.getPlayer());
        }
    }
}
