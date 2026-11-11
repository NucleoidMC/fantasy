package xyz.nucleoid.fantasy.mixin;

import net.minecraft.world.level.gamerules.GameRuleMap;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.level.gamerules.GameRules.class)
public interface GameRulesAccessor {
    @Accessor
    GameRuleMap getRules();
}
