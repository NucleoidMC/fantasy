package xyz.nucleoid.fantasy.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.Fantasy;

public final class PlayerSnapshot {
    private final RegistryKey<World> dimension;
    private final Vec3d position;
    private final float yaw;
    private final float pitch;
    private final CompoundTag playerData;

    private PlayerSnapshot(RegistryKey<World> dimension, Vec3d position, float yaw, float pitch, CompoundTag playerData) {
        this.dimension = dimension;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.playerData = playerData;
    }

    public static PlayerSnapshot take(ServerPlayerEntity player) {
        RegistryKey<World> dimension = player.world.getRegistryKey();
        Vec3d position = player.getPos();
        float yaw = player.yaw;
        float pitch = player.pitch;

        CompoundTag playerData = player.toTag(new CompoundTag());

        return new PlayerSnapshot(dimension, position, yaw, pitch, playerData);
    }

    public void restore(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld().getServer().getWorld(this.dimension);

        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(world, this.position.x, this.position.y, this.position.z, this.yaw, this.pitch);

        player.fromTag(this.playerData);

        // force synchronize the updated gamemode
        player.setGameMode(player.interactionManager.getGameMode());

        Fantasy.resetPlayer(player);
    }
}
