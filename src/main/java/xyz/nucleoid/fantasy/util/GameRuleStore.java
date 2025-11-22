package xyz.nucleoid.fantasy.util;

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.rule.GameRule;
import org.jetbrains.annotations.Nullable;

public final class GameRuleStore {
    private final Reference2BooleanMap<GameRule<Boolean>> booleanRules = new Reference2BooleanOpenHashMap<>();
    private final Reference2IntMap<GameRule<Integer>> intRules = new Reference2IntOpenHashMap<>();

    public void set(GameRule<Boolean> key, boolean value) {
        this.booleanRules.put(key, value);
    }

    public void set(GameRule<Integer> key, int value) {
        this.intRules.put(key, value);
    }

    public boolean getBoolean(GameRule<Boolean> key) {
        return this.booleanRules.getBoolean(key);
    }

    public int getInt(GameRule<Integer> key) {
        return this.intRules.getInt(key);
    }

    public boolean contains(GameRule<?> key) {
        return this.booleanRules.containsKey(key) || this.intRules.containsKey(key);
    }

    public void applyTo(GameRules rules, @Nullable MinecraftServer server) {
        Reference2BooleanMaps.fastForEach(this.booleanRules, entry -> {
            rules.setValue(entry.getKey(), entry.getBooleanValue(), server);
        });

        Reference2IntMaps.fastForEach(this.intRules, entry -> {
        	rules.setValue(entry.getKey(), entry.getIntValue(), server);
        });
    }
}
