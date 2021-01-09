package xyz.nucleoid.fantasy.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.BubbleWorldConfig;

public final class BubbledServerPlayerEntity extends ServerPlayerEntity {
    private final BubbleWorldConfig bubbleConfig;

    public BubbledServerPlayerEntity(MinecraftServer server, ServerWorld world, GameProfile profile, ServerPlayerInteractionManager interactionManager, BubbleWorldConfig bubbleConfig) {
        super(server, world, profile, interactionManager);
        this.bubbleConfig = bubbleConfig;
    }

    @Override
    public void teleport(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch) {
        if (targetWorld != this.world) {
            PlayerTeleporter teleporter = new PlayerTeleporter(this);
            teleporter.teleportFromBubbleTo(this.bubbleConfig, targetWorld);
        }
        super.teleport(targetWorld, x, y, z, yaw, pitch);
    }

    @Nullable
    @Override
    public Entity moveToWorld(ServerWorld targetWorld) {
        PlayerTeleporter teleporter = new PlayerTeleporter(this);
        ServerPlayerEntity player = teleporter.teleportFromBubbleTo(this.bubbleConfig, targetWorld);
        return player.moveToWorld(targetWorld);
    }
}
