package xyz.nucleoid.fantasy.mixin.bubble;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.nucleoid.fantasy.BubbleAccess;
import xyz.nucleoid.fantasy.player.PlayerManagerAccess;
import xyz.nucleoid.fantasy.util.PlayerResetter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin implements PlayerManagerAccess {
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow
    @Final
    private WorldSaveHandler saveHandler;
    @Shadow
    @Final
    private Map<UUID, ServerStatHandler> statisticsMap;
    @Shadow
    @Final
    private Map<UUID, PlayerAdvancementTracker> advancementTrackers;

    @Shadow
    protected abstract void savePlayerData(ServerPlayerEntity player);

    @Shadow
    protected abstract void setGameMode(ServerPlayerEntity player, @Nullable ServerPlayerEntity oldPlayer, ServerWorld world);

    @Shadow
    public abstract void sendCommandTree(ServerPlayerEntity player);

    @Shadow
    public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);

    @Shadow
    public abstract CompoundTag getUserData();

    @Unique
    private PlayerResetter playerResetter;

    @Inject(
            method = "respawnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void respawnPlayer(
            ServerPlayerEntity oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> ci,
            BlockPos spawnPos, float spawnAngle, boolean spawnSet, ServerWorld spawnWorld, Optional<Vec3d> respawnPoint,
            ServerPlayerInteractionManager interactionManager, ServerWorld respawnWorld, ServerPlayerEntity respawnedPlayer
    ) {
        if (BubbleAccess.isPlayedBubbled(oldPlayer)) {
            this.loadIntoPlayer(respawnedPlayer);
            respawnedPlayer.setWorld(respawnWorld);
            interactionManager.setWorld(respawnWorld);

            // this is later used to apply back to the respawned player, and we want to maintain that
            oldPlayer.interactionManager.setGameMode(interactionManager.getGameMode(), interactionManager.getPreviousGameMode());
        }
    }

    @Override
    public void teleportAndRecreate(ServerPlayerEntity player, Function<ServerPlayerEntity, ServerWorld> recreate) {
        player.detach();
        this.savePlayerData(player);

        player.getAdvancementTracker().clearCriteria();
        this.server.getBossBarManager().onPlayerDisconnect(player);

        player.getServerWorld().removePlayer(player);
        player.removed = false;

        PlayerResetter resetter = this.getPlayerResetter();
        resetter.apply(player);

        ServerWorld world = recreate.apply(player);
        player.setWorld(world);
        player.interactionManager.setWorld(world);

        WorldProperties worldProperties = world.getLevelProperties();

        ServerPlayNetworkHandler networkHandler = player.networkHandler;
        networkHandler.sendPacket(new PlayerRespawnS2CPacket(
                world.getDimension(), world.getRegistryKey(),
                BiomeAccess.hashSeed(world.getSeed()),
                player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(),
                world.isDebugWorld(), world.isFlat(), false
        ));

        networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.abilities));
        networkHandler.sendPacket(new HeldItemChangeS2CPacket(player.inventory.selectedSlot));

        this.sendCommandTree(player);
        player.getRecipeBook().sendInitRecipesPacket(player);

        world.onPlayerTeleport(player);
        networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch);

        this.server.getBossBarManager().onPlayerConnect(player);

        this.sendWorldInfo(player, world);

        for (StatusEffectInstance effect : player.getStatusEffects()) {
            networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getEntityId(), effect));
        }
    }

    @Override
    public void loadIntoPlayer(ServerPlayerEntity player) {
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
    }

    @Unique
    private RegistryKey<World> getDimensionFromData(CompoundTag playerData) {
        return DimensionType.method_28521(new Dynamic<>(NbtOps.INSTANCE, playerData.get("Dimension")))
                .resultOrPartial(LOGGER::error)
                .orElse(World.OVERWORLD);
    }

    @Inject(method = "savePlayerData", at = @At("HEAD"), cancellable = true)
    private void savePlayerData(ServerPlayerEntity player, CallbackInfo ci) {
        if (BubbleAccess.isPlayedBubbled(player)) {
            ci.cancel();
        }
    }

    @Override
    public PlayerResetter getPlayerResetter() {
        if (this.playerResetter == null) {
            ServerWorld overworld = this.server.getOverworld();
            GameProfile profile = new GameProfile(Util.NIL_UUID, "null");
            ServerPlayerInteractionManager interactionManager = new ServerPlayerInteractionManager(overworld);

            ServerPlayerEntity player = new ServerPlayerEntity(this.server, overworld, profile, interactionManager);
            this.statisticsMap.remove(Util.NIL_UUID);
            this.advancementTrackers.remove(Util.NIL_UUID);

            CompoundTag tag = new CompoundTag();
            player.toTag(tag);
            tag.remove("UUID");
            tag.remove("Pos");

            this.playerResetter = new PlayerResetter(tag);
        }

        return this.playerResetter;
    }
}
