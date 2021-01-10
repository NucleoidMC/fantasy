package xyz.nucleoid.fantasy.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.fantasy.BubbleWorldConfig;

import java.util.function.Function;

public final class BubblePlayerTeleporter {
    private static final String EMPTY_RESOURCE_PACK_URL = "https://nucleoid.xyz/resources/empty_resource_pack.zip";
    private static final String EMPTY_RESOURCE_PACK_HASH = "B740E5E6C39C0549D05A1F979156B1FE6A03D9BF";

    private final ServerPlayerEntity player;
    private final MinecraftServer server;
    private final PlayerManagerAccess playerManager;

    public BubblePlayerTeleporter(ServerPlayerEntity player) {
        this.player = player;
        this.server = player.server;
        this.playerManager = (PlayerManagerAccess) this.server.getPlayerManager();
    }

    public void teleportIntoBubble(ServerWorld world, BubbleWorldConfig bubbleConfig) {
        this.teleportAndRecreate(player -> {
            player.interactionManager.setGameMode(bubbleConfig.getDefaultGameMode());
            bubbleConfig.getSpawner().spawnPlayer(world, player);
            return world;
        });

        this.onJoinBubble(bubbleConfig);
    }

    public void teleportFromBubbleTo(BubbleWorldConfig bubbleConfig, ServerWorld world) {
        this.teleportAndRecreate(player -> {
            this.playerManager.loadIntoPlayer(player);
            return world;
        });

        this.onLeaveBubble(bubbleConfig);
    }

    public void teleportFromBubble(BubbleWorldConfig bubbleConfig) {
        this.teleportAndRecreate(player -> {
            this.playerManager.loadIntoPlayer(player);
            return player.getServerWorld();
        });

        this.onLeaveBubble(bubbleConfig);
    }

    private void teleportAndRecreate(Function<ServerPlayerEntity, ServerWorld> recreator) {
        PlayerManagerAccess playerManagerAccess = (PlayerManagerAccess) this.server.getPlayerManager();
        playerManagerAccess.teleportAndRecreate(this.player, recreator);
    }

    private void onJoinBubble(BubbleWorldConfig bubbleConfig) {
        String resourcePackUrl = bubbleConfig.getResourcePackUrl();
        if (resourcePackUrl != null) {
            this.player.sendResourcePackUrl(resourcePackUrl, bubbleConfig.getResourcePackHash());
        }
    }

    private void onLeaveBubble(BubbleWorldConfig bubbleConfig) {
        if (!this.server.getResourcePackUrl().isEmpty()) {
            this.player.sendResourcePackUrl(this.server.getResourcePackUrl(), this.server.getResourcePackHash());
        } else if (bubbleConfig.getResourcePackUrl() != null) {
            this.sendEmptyResourcePack(this.player);
        }
    }

    private void sendEmptyResourcePack(ServerPlayerEntity player) {
        player.sendResourcePackUrl(EMPTY_RESOURCE_PACK_URL, EMPTY_RESOURCE_PACK_HASH);
    }
}
