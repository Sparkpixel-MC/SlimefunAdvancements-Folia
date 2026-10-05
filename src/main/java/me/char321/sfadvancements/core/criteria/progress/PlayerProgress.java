package me.char321.sfadvancements.core.criteria.progress;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import me.char321.sfadvancements.SFAdvancements;
import me.char321.sfadvancements.api.Advancement;
import me.char321.sfadvancements.api.criteria.Criterion;
import me.char321.sfadvancements.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * a per-player object that stores their advancement progress <br>
 *
 * json <br>
 *
 */
public class PlayerProgress {
    private final UUID player;
    // criteria of the same advancement may be completed from different region threads at once
    private final Map<NamespacedKey, AdvancementProgress> progressMap = new ConcurrentHashMap<>();

    private PlayerProgress(UUID player) {
        this.player = player;
    }

    public static PlayerProgress get(Player player) {
        return get(player.getUniqueId());
    }

    public static PlayerProgress get(UUID player) {
        PlayerProgress res = new PlayerProgress(player);

        File advancementsFolder = new File(SFAdvancements.instance().getDataFolder(), "/advancements");
        File f = new File(advancementsFolder, player.toString() + ".json");
        if (f.exists()) {
            try {
                JsonObject object = JsonParser.parseReader(new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))).getAsJsonObject();
                res.loadFromObject(object);
            } catch (IOException e) {
                SFAdvancements.logger().log(Level.SEVERE, "读取进度时发生错误", e);
            }
        }
        return res;
    }

    public void doCriterion(Criterion criterion) {
        NamespacedKey adv = criterion.getAdvancement();
        AdvancementProgress advProgress = progressMap.computeIfAbsent(adv, AdvancementProgress::new);
        if (advProgress.done.get()) {
            return;
        }

        for (CriteriaProgress progress : advProgress.criteria) {
            if (!progress.id.equals(criterion.getId())) {
                continue;
            }

            if (progress.done.get()) {
                continue;
            }

            int newProgress = progress.progress.updateAndGet(v -> Math.min(v + 1, criterion.getCount()));
            if (newProgress >= criterion.getCount()) {
                progress.done.set(true);
                advProgress.checkComplete();
            }
        }
    }

    public void completeCriterion(Criterion criterion) {
        NamespacedKey adv = criterion.getAdvancement();
        AdvancementProgress progress = progressMap.computeIfAbsent(adv, AdvancementProgress::new);

        for (CriteriaProgress criteriaProgress : progress.criteria) {
            if (!criteriaProgress.id.equals(criterion.getId())) {
                continue;
            }

            if (criteriaProgress.done.get()) {
                return;
            }

            criteriaProgress.done.set(true);
            criteriaProgress.progress.set(criterion.getCount());
            progress.checkComplete();
        }
    }

    public int getCriterionProgress(Criterion cri) {
        NamespacedKey adv = cri.getAdvancement();
        if (!progressMap.containsKey(adv)) {
            return 0;
        }

        AdvancementProgress advProgress = progressMap.get(adv);
        for (CriteriaProgress progress : advProgress.criteria) {
            if (progress.id.equals(cri.getId())) {
                return progress.progress.get();
            }
        }
        throw new IllegalStateException();
    }

    public boolean revokeAdvancement(NamespacedKey adv) {
        if (!progressMap.containsKey(adv)) {
            return false;
        }
        AdvancementProgress progress = progressMap.get(adv);
        progress.done.set(false);
        for (CriteriaProgress criteriaProgress : progress.criteria) {
            criteriaProgress.done.set(false);
            criteriaProgress.progress.set(0);
        }
        Player playerEntity = Bukkit.getPlayer(player);
        if (playerEntity != null) {
            Utils.fromKey(adv).revoke(playerEntity);
        }
        return true;
    }

    public List<NamespacedKey> getCompletedAdvancements() {
        List<NamespacedKey> res = new ArrayList<>();
        for (Map.Entry<NamespacedKey, AdvancementProgress> entry : progressMap.entrySet()) {
            if (entry.getValue().done.get()) {
                res.add(entry.getKey());
            }
        }
        return res;
    }

    private void loadFromObject(JsonObject object) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            NamespacedKey advkey = NamespacedKey.fromString(entry.getKey());
            if(!Utils.isValidAdvancement(advkey)) {
                SFAdvancements.warn("未知进度: " + advkey);
                continue;
            }
            AdvancementProgress newprogress = new AdvancementProgress(advkey);
            progressMap.put(advkey, newprogress);
            newprogress.loadFromObject(entry.getValue().getAsJsonObject());
        }
    }

    public void save() throws IOException {
        File advancementsFolder = new File(SFAdvancements.instance().getDataFolder(), "/advancements");
        File f = new File(advancementsFolder, player +".json");
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            if (!f.createNewFile()) {
                throw new IOException("无法创建文件 " + f.getPath());
            }
        }

        try(JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, false), StandardCharsets.UTF_8)))) {
            writer.beginObject();
            for (Map.Entry<NamespacedKey, AdvancementProgress> entry : progressMap.entrySet()) {
                writer.name(entry.getKey().toString());
                writer.beginObject();
                writer.name("done").value(entry.getValue().done.get());
                writer.name("criteria");
                writer.beginObject();
                for (CriteriaProgress criterion : entry.getValue().criteria) {
                    writer.name(criterion.id).value(criterion.progress.get());
                }
                writer.endObject();
                writer.endObject();
            }
            writer.endObject();
        }
    }

    /**
     * determines if a given advancement is completed for this player progress
     *
     * @param key the key of the advancement
     * @return if the advancement is completed
     */
    public boolean isCompleted(NamespacedKey key) {
        if (!progressMap.containsKey(key)) {
            return false;
        }
        AdvancementProgress prog = progressMap.get(key);
        return prog.done.get();
    }

    class AdvancementProgress {
        Advancement adv;
        final AtomicBoolean done = new AtomicBoolean(false);
        CriteriaProgress[] criteria;

        AdvancementProgress(NamespacedKey adv) {
            this(Utils.fromKey(adv));
        }

        AdvancementProgress(Advancement adv) {
            this.adv = adv;
            this.criteria = new CriteriaProgress[adv.getCriteria().length];
            for (int i = 0; i < adv.getCriteria().length; i++) {
                criteria[i] = new CriteriaProgress(adv.getCriteria()[i].getId());
            }
        }

        /**
         * marks the advancement complete if every criterion is done;
         * the CAS guarantees onComplete (and its rewards) run exactly once
         */
        void checkComplete() {
            for (CriteriaProgress criterion : criteria) {
                if (!criterion.done.get()) {
                    return;
                }
            }
            if (done.compareAndSet(false, true)) {
                Player p = Bukkit.getPlayer(player);
                if (p == null) {
                    SFAdvancements.warn("玩家 " + player + " 已离线，跳过进度奖励: " + adv.getKey());
                    return;
                }
                adv.onComplete(p);
            }
        }

        void loadFromObject(JsonObject object) {
            done.set(object.get("done").getAsBoolean());
            JsonObject jsonCriteria = object.get("criteria").getAsJsonObject();
            criteria = new CriteriaProgress[adv.getCriteria().length];
            int i = 0;
            for (Criterion criterion : adv.getCriteria()) {
                CriteriaProgress criteriaProgress;
                JsonElement element = jsonCriteria.get(criterion.getId());
                if (element == null || !element.isJsonPrimitive()) {
                    criteriaProgress = new CriteriaProgress(criterion.getId(), 0);
                } else {
                    int progress = element.getAsInt();
                    criteriaProgress = new CriteriaProgress(criterion.getId(), progress);
                    criteriaProgress.done.set(progress >= criterion.getCount());
                }
                criteria[i] = criteriaProgress;
                i++;
            }
        }
    }

    static class CriteriaProgress {
        final String id;
        final AtomicBoolean done = new AtomicBoolean(false);
        final AtomicInteger progress = new AtomicInteger(0);

        CriteriaProgress(String id) {
            this(id, 0);
        }

        CriteriaProgress(String id, int progress) {
            this.id = id;
            this.progress.set(progress);
        }
    }
}
