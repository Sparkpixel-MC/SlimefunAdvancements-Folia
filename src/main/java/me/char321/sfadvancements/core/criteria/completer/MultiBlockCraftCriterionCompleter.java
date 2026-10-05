package me.char321.sfadvancements.core.criteria.completer;

import io.github.thebusybiscuit.slimefun4.api.events.MultiBlockCraftEvent;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.char321.sfadvancements.SFAdvancements;
import me.char321.sfadvancements.api.criteria.Criterion;
import me.char321.sfadvancements.api.criteria.MultiBlockCraftCriterion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiBlockCraftCriterionCompleter implements CriterionCompleter, Listener {
    private final Map<Material, Set<MultiBlockCraftCriterion>> criteria = new ConcurrentHashMap<>();
    private final Set<MultiBlockCraftCriterion> nonMaterialCriteria = ConcurrentHashMap.newKeySet();

    public MultiBlockCraftCriterionCompleter() {
        Bukkit.getPluginManager().registerEvents(this, SFAdvancements.instance());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMultiBlockCraft(MultiBlockCraftEvent e) {
        performCriteria(e.getPlayer(), e.getMachine().getId(), e.getOutput());
    }

    private void performCriteria(Player player, String machineId, ItemStack output) {
        for (MultiBlockCraftCriterion criterion : nonMaterialCriteria) {
            String machineId1 = criterion.getMachineId();
            if (machineId1 == null || machineId1.equals(machineId)) {
                criterion.perform(player);
            }
        }

        Set<MultiBlockCraftCriterion> allcriteria = criteria.get(output.getType());
        if (allcriteria == null) {
            return;
        }

        for (MultiBlockCraftCriterion criterion : allcriteria) {
            String machineId1 = criterion.getMachineId();
            if ((machineId1 == null || machineId1.equals(machineId)) &&
                    SlimefunUtils.isItemSimilar(output, criterion.getItem(), false, false)) {
                criterion.perform(player);
            }
        }
    }

    @Override
    public void register(Criterion criterion) {
        if (!(getCriterionClass().isInstance(criterion))) {
            throw new IllegalArgumentException("criterion must be an " + getCriterionClass().getName());
        }

        MultiBlockCraftCriterion criterion1 = ((MultiBlockCraftCriterion) criterion);
        ItemStack item = criterion1.getItem();
        if (item == null) {
            nonMaterialCriteria.add(criterion1);
        } else {
            criteria.computeIfAbsent(item.getType(), k -> ConcurrentHashMap.newKeySet()).add(criterion1);
        }
    }

    @Override
    public Class<? extends Criterion> getCriterionClass() {
        return MultiBlockCraftCriterion.class;
    }

    @Override
    public void reload() {
        criteria.clear();
        nonMaterialCriteria.clear();
    }
}
