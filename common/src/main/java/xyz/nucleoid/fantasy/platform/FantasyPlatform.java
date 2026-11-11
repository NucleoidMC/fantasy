package xyz.nucleoid.fantasy.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

/**
 * Mod loader specific operations needed by the common code.
 * <p>
 * Each loader provides one implementation, registered in
 * {@code META-INF/services/xyz.nucleoid.fantasy.platform.FantasyPlatform}.
 */
@ApiStatus.Internal
public interface FantasyPlatform {
    FantasyPlatform INSTANCE = ServiceLoader.load(FantasyPlatform.class, FantasyPlatform.class.getClassLoader())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No " + FantasyPlatform.class.getName() + " implementation found"));

    /**
     * Notifies the mod loader that a runtime level has been added to the server.
     */
    void onLevelLoad(MinecraftServer server, ServerLevel level);

    /**
     * Notifies the mod loader that a runtime level has been removed from the server.
     */
    void onLevelUnload(MinecraftServer server, ServerLevel level);
}
