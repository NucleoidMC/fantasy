package xyz.nucleoid.fantasy;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public final class RuntimeWorldHandle {
    private final Fantasy fantasy;
    private final ServerWorld world;

    RuntimeWorldHandle(Fantasy fantasy, ServerWorld world) {
        this.fantasy = fantasy;
        this.world = world;
    }

    public void setTickWhenEmpty(boolean tickWhenEmpty) {
        ((FantasyWorldAccess) this.world).fantasy$setTickWhenEmpty(tickWhenEmpty);
    }

    public void delete() {
        this.fantasy.enqueueWorldDeletion(this.world);
    }

    public ServerWorld asWorld() {
        return this.world;
    }

    public RegistryKey<World> getRegistryKey() {
        return this.world.getRegistryKey();
    }
}
