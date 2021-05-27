package xyz.nucleoid.fantasy;

import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

final class DelayedWorldQueue {
    private final List<Entry> queue = new ArrayList<>();

    void submit(ServerWorld world, CompletableFuture<RuntimeWorldHandle> future) {
        this.queue.add(new Entry(world, future, 1));
    }

    void tick(Fantasy fantasy) {
        List<Entry> queue = this.queue;
        if (!queue.isEmpty()) {
            queue.removeIf(entry -> entry.complete(fantasy));
        }
    }

    static final class Entry {
        final ServerWorld world;
        final CompletableFuture<RuntimeWorldHandle> future;
        final long yieldTime;

        Entry(ServerWorld world, CompletableFuture<RuntimeWorldHandle> future, long delay) {
            this.world = world;
            this.future = future;
            this.yieldTime = world.getTime() + delay;
        }

        boolean complete(Fantasy fantasy) {
            if (this.world.getTime() >= this.yieldTime) {
                this.future.complete(new RuntimeWorldHandle(fantasy, this.world));
                return true;
            } else {
                return false;
            }
        }
    }
}
