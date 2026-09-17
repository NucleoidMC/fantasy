package xyz.nucleoid.fantasy;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.clock.ClockNetworkState;
import net.minecraft.world.clock.PackedClockStates;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class RuntimeClockManager extends ServerClockManager {
    protected final BooleanSupplier advanceTime;
    protected MinecraftServer server;

    public RuntimeClockManager(PackedClockStates packedClockStates, BooleanSupplier advanceTime) {
        super(packedClockStates);
        this.advanceTime = advanceTime;
    }

    @Override
    public void init(MinecraftServer server) {
        super.init(server);
        this.server = server;
    }

    @Override
    public void tick() {
        if (this.advanceTime.getAsBoolean()) {
            ((ServerClockManagerExtension) this).fantasy$getClocks().values().forEach(ServerClockInstance::tick);
            this.setDirty();
        }
    }

    @Override
    protected void modifyClock(final Holder<WorldClock> clock, final Consumer<? super ServerClockInstance> action) {
        ServerClockInstance instance = this.getInstance(clock);
        action.accept(instance);
        Map<Holder<WorldClock>, ClockNetworkState> updates = Map.of(clock, this.packNetworkState(instance, this.server));
        this.setDirty();

        var packet = new ClientboundSetTimePacket(this.getGameTime(), updates);

        for (ServerLevel level : this.server.getAllLevels()) {
            if (level.clockManager() == this) {
                for (var player : level.players()) {
                    player.connection.send(packet);
                }

                level.environmentAttributes().invalidateTickCache();
            }
        }
    }

    @Override
    public ClientboundSetTimePacket createFullSyncPacket() {
        return new ClientboundSetTimePacket(this.getGameTime(), Util.mapValues(((ServerClockManagerExtension) this).fantasy$getClocks(), (clock) -> this.packNetworkState(clock, this.server)));
    }

    protected ClockNetworkState packNetworkState(ServerClockInstance instance, final MinecraftServer server) {
        boolean paused = instance.isPaused() || !this.advanceTime.getAsBoolean();
        return new ClockNetworkState(instance.totalTicks(), instance.partialTick(), paused ? 0.0F : instance.rate());
    }

    public void tickFromLevel(RuntimeLevel level) {
        this.tick();
    }
}
