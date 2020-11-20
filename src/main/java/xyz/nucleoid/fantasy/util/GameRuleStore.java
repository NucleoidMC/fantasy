package xyz.nucleoid.fantasy.util;

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

public final class GameRuleStore {
    private final Reference2BooleanMap<GameRules.Key<GameRules.BooleanRule>> booleanRules = new Reference2BooleanOpenHashMap<>();
    private final Reference2IntMap<GameRules.Key<GameRules.IntRule>> intRules = new Reference2IntOpenHashMap<>();

    public void set(GameRules.Key<GameRules.BooleanRule> key, boolean value) {
        this.booleanRules.put(key, value);
    }

    public void set(GameRules.Key<GameRules.IntRule> key, int value) {
        this.intRules.put(key, value);
    }

    public void applyTo(GameRules rules, @Nullable MinecraftServer server) {
        Reference2BooleanMaps.fastForEach(this.booleanRules, entry -> {
            GameRules.BooleanRule rule = rules.get(entry.getKey());
            rule.set(entry.getBooleanValue(), server);
        });

        Reference2IntMaps.fastForEach(this.intRules, entry -> {
            GameRules.IntRule rule = rules.get(entry.getKey());
            rule.value = entry.getIntValue();
        });
    }
}
