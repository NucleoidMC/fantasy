package xyz.nucleoid.fantasy;

import com.google.common.collect.Streams;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;
import xyz.nucleoid.fantasy.mixin.GameRulesAccessor;

import java.util.List;
import java.util.stream.Stream;

class DelegatingGameRules extends GameRules {
    private final GameRules parent;
    private final GameRuleMap rules;

    public DelegatingGameRules(GameRules parent) {
        super(List.of());
        this.parent = parent;
        this.rules = ((GameRulesAccessor) this).getRules();
    }

    @Override
    public <T> void set(GameRule<T> gameRule, T value, @Nullable MinecraftServer server) {
        this.rules.set(gameRule, value);
        if (server != null) {
            server.onGameRuleChanged(gameRule, value);
        }
    }

    @Override
    public <T> T get(GameRule<T> gameRule) {
        T value = this.rules.get(gameRule);
        if (value == null) {
            return this.parent.get(gameRule);
        } else {
            return value;
        }
    }

    @Override
    public Stream<GameRule<?>> availableRules() {
        return Streams.concat(this.rules.keySet().stream(), this.parent.availableRules());
    }

    @Override
    public GameRules copy(FeatureFlagSet enabledFeatures) {
        var copy = new DelegatingGameRules(this.parent);
        this.setAll(this, null);
        return copy;
    }
}
