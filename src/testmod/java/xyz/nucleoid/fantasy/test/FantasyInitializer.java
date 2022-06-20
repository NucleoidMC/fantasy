package xyz.nucleoid.fantasy.test;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

public final class FantasyInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((s) -> {
            Fantasy.get(s).openTemporaryWorld(new RuntimeWorldConfig().setGenerator(new VoidChunkGenerator(s.getRegistryManager().get(Registry.BIOME_KEY).getEntry(0).get())));
            Fantasy.get(s).getOrOpenPersistentWorld(
                    new Identifier("fantasytest:test"),
                    new RuntimeWorldConfig()
                            .setGenerator(s.getOverworld().getChunkManager().getChunkGenerator())
            );
        });
    }

}
