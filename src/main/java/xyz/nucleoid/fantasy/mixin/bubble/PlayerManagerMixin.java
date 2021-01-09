package xyz.nucleoid.fantasy.mixin.bubble;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.fantasy.player.BubbledServerPlayerEntity;
import xyz.nucleoid.fantasy.player.PlayerManagerAccess;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin implements PlayerManagerAccess {
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private List<ServerPlayerEntity> players;
    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow
    @Final
    private Map<UUID, ServerPlayerEntity> playerMap;

    @Shadow
    protected abstract void savePlayerData(ServerPlayerEntity player);

    @Shadow
    protected abstract void setGameMode(ServerPlayerEntity player, @Nullable ServerPlayerEntity oldPlayer, ServerWorld world);

    @Shadow
    public abstract void sendCommandTree(ServerPlayerEntity player);

    @Shadow
    protected abstract void sendScoreboard(ServerScoreboard scoreboard, ServerPlayerEntity player);

    @Shadow
    public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);

    @Shadow
    public abstract CompoundTag getUserData();

    @Shadow
    @Final
    private WorldSaveHandler saveHandler;

    @Override
    public void teleportAndRecreate(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, ServerWorld world) {
        // make sure we're being called with the most up-to-date player entity
        oldPlayer = this.playerMap.getOrDefault(oldPlayer.getUuid(), oldPlayer);

        oldPlayer.detach();
        this.savePlayerData(oldPlayer);

        this.players.remove(oldPlayer);

        oldPlayer.getServerWorld().removePlayer(oldPlayer);

        oldPlayer.getAdvancementTracker().clearCriteria();
        this.server.getBossBarManager().onPlayerDisconnect(oldPlayer);

        WorldProperties worldProperties = world.getLevelProperties();

        ServerPlayNetworkHandler networkHandler = oldPlayer.networkHandler;
        networkHandler.player = newPlayer;

        newPlayer.networkHandler = networkHandler;

        networkHandler.sendPacket(new PlayerRespawnS2CPacket(
                world.getDimension(), world.getRegistryKey(),
                BiomeAccess.hashSeed(world.getSeed()),
                newPlayer.interactionManager.getGameMode(), newPlayer.interactionManager.getPreviousGameMode(),
                world.isDebugWorld(), world.isFlat(), false
        ));

        this.players.add(newPlayer);
        this.playerMap.put(oldPlayer.getUuid(), newPlayer);

        networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(newPlayer.abilities));
        networkHandler.sendPacket(new HeldItemChangeS2CPacket(newPlayer.inventory.selectedSlot));

        this.sendCommandTree(newPlayer);
        newPlayer.getStatHandler().updateStatSet();
        newPlayer.getRecipeBook().sendInitRecipesPacket(newPlayer);
        this.sendScoreboard(world.getScoreboard(), newPlayer);

        networkHandler.requestTeleport(newPlayer.getX(), newPlayer.getY(), newPlayer.getZ(), newPlayer.yaw, newPlayer.pitch);

        world.onPlayerConnected(newPlayer);
        this.server.getBossBarManager().onPlayerConnect(newPlayer);

        this.sendWorldInfo(newPlayer, world);

        for (StatusEffectInstance effect : newPlayer.getStatusEffects()) {
            networkHandler.sendPacket(new EntityStatusEffectS2CPacket(newPlayer.getEntityId(), effect));
        }

        newPlayer.onSpawn();

        // if anything still is referencing the old player, the best we can do is have it be up-to-date
        oldPlayer.world = newPlayer.world;
        oldPlayer.copyFrom(newPlayer);

        // close the "joining world" screen
        networkHandler.sendPacket(new CloseScreenS2CPacket());
    }

    @Override
    public ServerPlayerEntity createLoadedPlayer(GameProfile profile) {
        ServerWorld overworld = this.server.getOverworld();
        ServerPlayerInteractionManager interactionManager = new ServerPlayerInteractionManager(overworld);
        ServerPlayerEntity player = new ServerPlayerEntity(this.server, overworld, profile, interactionManager);

        CompoundTag userData = this.getUserData();
        if (userData == null) {
            userData = this.server.getSaveProperties().getPlayerData();
        }

        CompoundTag playerData;
        if (player.getName().getString().equals(this.server.getUserName()) && userData != null) {
            playerData = userData;
            player.fromTag(userData);
        } else {
            playerData = this.saveHandler.loadPlayerData(player);
        }

        RegistryKey<World> dimension = playerData != null ? this.getDimensionFromData(playerData) : null;

        ServerWorld world = this.server.getWorld(dimension);
        if (world == null) {
            world = this.server.getOverworld();
        }

        player.setWorld(world);
        player.interactionManager.setWorld(world);

        this.setGameMode(player, null, world);

        return player;
    }

    @Unique
    private RegistryKey<World> getDimensionFromData(CompoundTag playerData) {
        return DimensionType.method_28521(new Dynamic<>(NbtOps.INSTANCE, playerData.get("Dimension")))
                .resultOrPartial(LOGGER::error)
                .orElse(World.OVERWORLD);
    }

    @Inject(method = "savePlayerData", at = @At("HEAD"), cancellable = true)
    private void savePlayerData(ServerPlayerEntity player, CallbackInfo ci) {
        if (player instanceof BubbledServerPlayerEntity) {
            ci.cancel();
        }
    }
}
