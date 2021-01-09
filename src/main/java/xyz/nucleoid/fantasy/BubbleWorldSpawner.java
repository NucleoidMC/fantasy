package xyz.nucleoid.fantasy;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;

public interface BubbleWorldSpawner {
    static BubbleWorldSpawner at(Vec3d pos) {
        return new At(pos);
    }

    static BubbleWorldSpawner atSurface(BlockPos pos) {
        return new Surface(pos);
    }

    static BubbleWorldSpawner atSurface(int x, int z) {
        return new Surface(new BlockPos(x, 0, z));
    }

    void spawnPlayer(ServerWorld world, ServerPlayerEntity player);

    final class At implements BubbleWorldSpawner {
        private final Vec3d pos;

        At(Vec3d pos) {
            this.pos = pos;
        }

        @Override
        public void spawnPlayer(ServerWorld world, ServerPlayerEntity player) {
            player.refreshPositionAndAngles(this.pos.x, this.pos.y, this.pos.z, 0.0F, 0.0F);
        }
    }

    final class Surface implements BubbleWorldSpawner {
        private final BlockPos pos;

        Surface(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public void spawnPlayer(ServerWorld world, ServerPlayerEntity player) {
            Chunk chunk = world.getChunk(this.pos);
            int surfaceY = chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, this.pos.getX(), this.pos.getZ());

            player.refreshPositionAndAngles(this.pos.getX() + 0.5, surfaceY + 1, this.pos.getZ() + 0.5, 0.0F, 0.0F);
        }
    }
}
