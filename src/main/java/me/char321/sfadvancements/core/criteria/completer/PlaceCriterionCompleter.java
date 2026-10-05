package me.char321.sfadvancements.core.criteria.completer;

import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.char321.sfadvancements.SFAdvancements;
import me.char321.sfadvancements.api.criteria.Criterion;
import me.char321.sfadvancements.api.criteria.PlaceCriterion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Set;

public class PlaceCriterionCompleter implements CriterionCompleter, Listener {
    //maybe i should made this under something but that would be kinda jank oh well
    private final Map<Material, Set<PlaceCriterion>> criteria = new ConcurrentHashMap<>();

    public PlaceCriterionCompleter() {
        Bukkit.getPluginManager().registerEvents(this, SFAdvancements.instance());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Material m = e.getItemInHand().getType();
        Set<PlaceCriterion> all = criteria.get(m);
        if (all == null) {
            return;
        }

        for (PlaceCriterion criterion : all) {
            if (SlimefunUtils.isItemSimilar(criterion.getItem(), e.getItemInHand(), false, false)) {
                criterion.perform(e.getPlayer());
            }
        }
    }

    @Override
    public Class<? extends Criterion> getCriterionClass() {
        return PlaceCriterion.class;
    }

    @Override
    public void register(Criterion criterion) {
        if (!(getCriterionClass().isInstance(criterion))) {
            throw new IllegalArgumentException("criterion must be a " + getCriterionClass().getName());
        }

        PlaceCriterion placeCriterion = (PlaceCriterion) criterion;
        Material m = placeCriterion.getItem().getType();
        criteria.computeIfAbsent(m, k -> ConcurrentHashMap.newKeySet()).add(placeCriterion);
    }

    @Override
    public void reload() {
        criteria.clear();
    }
}
