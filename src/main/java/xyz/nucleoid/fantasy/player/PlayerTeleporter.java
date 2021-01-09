package xyz.nucleoid.fantasy.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.fantasy.BubbleWorldConfig;

public final class PlayerTeleporter {
    private static final String EMPTY_RESOURCE_PACK_URL = "https://nucleoid.xyz/resources/empty_resource_pack.zip";
    private static final String EMPTY_RESOURCE_PACK_HASH = "B740E5E6C39C0549D05A1F979156B1FE6A03D9BF";

    private final ServerPlayerEntity oldPlayer;
    private final MinecraftServer server;

    public PlayerTeleporter(ServerPlayerEntity player) {
        this.oldPlayer = player;
        this.server = player.server;
    }

    public ServerPlayerEntity teleportIntoBubble(ServerWorld world, BubbleWorldConfig bubbleConfig) {
        ServerPlayerEntity newPlayer = this.createPlayerForBubble(world, this.oldPlayer.getGameProfile(), bubbleConfig);
        this.teleportAndRecreate(newPlayer, world);

        this.onJoinBubble(newPlayer, bubbleConfig);

        return newPlayer;
    }

    public ServerPlayerEntity teleportFromBubbleTo(BubbleWorldConfig bubbleConfig, ServerWorld world) {
        ServerPlayerEntity newPlayer = this.createLoadedPlayer(this.oldPlayer.getGameProfile());
        this.teleportAndRecreate(newPlayer, world);

        this.onLeaveBubble(bubbleConfig, newPlayer);

        return newPlayer;
    }

    public ServerPlayerEntity teleportFromBubble(BubbleWorldConfig bubbleConfig) {
        ServerPlayerEntity newPlayer = this.createLoadedPlayer(this.oldPlayer.getGameProfile());
        this.teleportAndRecreate(newPlayer, newPlayer.getServerWorld());

        this.onLeaveBubble(bubbleConfig, newPlayer);

        return newPlayer;
    }

    private BubbledServerPlayerEntity createPlayerForBubble(ServerWorld world, GameProfile profile, BubbleWorldConfig config) {
        ServerPlayerInteractionManager interactionManager = new ServerPlayerInteractionManager(world);
        BubbledServerPlayerEntity player = new BubbledServerPlayerEntity(this.server, world, profile, interactionManager, config);

        player.interactionManager.setGameMode(config.getDefaultGameMode());
        config.getSpawner().spawnPlayer(world, player);

        return player;
    }

    private ServerPlayerEntity createLoadedPlayer(GameProfile profile) {
        PlayerManagerAccess playerManagerAccess = (PlayerManagerAccess) this.server.getPlayerManager();
        return playerManagerAccess.createLoadedPlayer(profile);
    }

    private void teleportAndRecreate(ServerPlayerEntity newPlayer, ServerWorld world) {
        PlayerManagerAccess playerManagerAccess = (PlayerManagerAccess) this.server.getPlayerManager();
        playerManagerAccess.teleportAndRecreate(this.oldPlayer, newPlayer, world);
    }

    private void onJoinBubble(ServerPlayerEntity player, BubbleWorldConfig config) {
        String resourcePackUrl = config.getResourcePackUrl();
        if (resourcePackUrl != null) {
            player.sendResourcePackUrl(resourcePackUrl, config.getResourcePackHash());
        }
    }

    private void onLeaveBubble(BubbleWorldConfig bubbleConfig, ServerPlayerEntity player) {
        if (!this.server.getResourcePackUrl().isEmpty()) {
            player.sendResourcePackUrl(this.server.getResourcePackUrl(), this.server.getResourcePackHash());
        } else if (bubbleConfig.getResourcePackUrl() != null) {
            this.sendEmptyResourcePack(player);
        }
    }

    private void sendEmptyResourcePack(ServerPlayerEntity player) {
        player.sendResourcePackUrl(EMPTY_RESOURCE_PACK_URL, EMPTY_RESOURCE_PACK_HASH);
    }
}
