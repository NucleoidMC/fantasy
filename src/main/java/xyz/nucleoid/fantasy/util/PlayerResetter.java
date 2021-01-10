package xyz.nucleoid.fantasy.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerResetter {
    private final CompoundTag resetTag;

    public PlayerResetter(CompoundTag resetTag) {
        this.resetTag = resetTag;
    }

    public void apply(ServerPlayerEntity player) {
        player.fromTag(this.resetTag);
        player.clearStatusEffects();
        player.getScoreboardTags().clear();
    }
}
