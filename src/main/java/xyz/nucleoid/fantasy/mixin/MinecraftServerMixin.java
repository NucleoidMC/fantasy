package xyz.nucleoid.fantasy.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.fantasy.MinecraftServerExtension;
import xyz.nucleoid.fantasy.RuntimeLevel;
import xyz.nucleoid.fantasy.RuntimeServerClockManager;
import xyz.nucleoid.fantasy.util.SafeIterator;

import java.net.Proxy;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExtension {
    @Shadow
    public abstract SavedDataStorage getDataStorage();

    @Shadow
    @Deprecated
    public abstract GameRules getGlobalGameRules();

    @Shadow
    @Final
    private GameRules gameRules;

    @Shadow
    public abstract ServerLevel overworld();

    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Shadow
    public abstract RegistryAccess.Frozen registryAccess();

    @Unique
    private RuntimeServerClockManager runtimeClockManager;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, Optional<GameRules> gameRules, Proxy proxy, DataFixer fixerUpper, Services services, LevelLoadListener levelLoadListener, boolean propagatesCrashes, CallbackInfo ci) {
        this.runtimeClockManager = this.getDataStorage().computeIfAbsent(RuntimeServerClockManager.TYPE);
        this.runtimeClockManager.init((MinecraftServer) (Object) this);
    }

    @Redirect(method = "tickChildren", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;", ordinal = 0), require = 0)
    private Iterator<ServerLevel> fantasy$copyBeforeTicking(Iterable<ServerLevel> instance) {
        return new SafeIterator<>((Collection<ServerLevel>) instance);
    }

    @Inject(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/clock/ServerClockManager;tick()V"))
    private void tickRuntimeClock(BooleanSupplier haveTime, CallbackInfo ci) {
        this.runtimeClockManager.tick();
    }

    @WrapOperation(method = "forceGameTimeSynchronization", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void forceGameRunTimeSynchronization(PlayerList instance, Packet<?> packet, Operation<Void> original) {
        instance.broadcastAll(packet, this.overworld().dimension());

        for (ServerLevel level : this.levels.values()) {
            if (level instanceof RuntimeLevel runtimeLevel) {
//                ClientboundSetTimePacket packet1 = new ClientboundSetTimePacket(runtimeLevel.getGameTime(), Map.of(this.registryAccess().getOrThrow(WorldClocks.OVERWORLD), runtimeLevel.clockManager().fantasy$getClocks().get(runtimeLevel.worldClock).packNetworkState(runtimeLevel.getServer())));
                ClientboundSetTimePacket packet1 = new ClientboundSetTimePacket(runtimeLevel.getGameTime(), Map.of());
                instance.broadcastAll(packet1, runtimeLevel.dimension());
            }
        }
    }

    @Override
    public RuntimeServerClockManager fantasy$clockManager() {
        return this.runtimeClockManager;
    }
}
