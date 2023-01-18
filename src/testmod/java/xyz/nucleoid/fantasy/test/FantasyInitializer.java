package xyz.nucleoid.fantasy.test;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.datafixer.Schemas;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.level.storage.LevelStorage;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.io.IOException;
import java.nio.file.Path;

public final class FantasyInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        var storage = new LevelStorage(
                Path.of("test-save"),
                Path.of("test-backup"),
                Schemas.getFixer()
        );
        try {
            var session = storage.createSession("test-session");
            ServerLifecycleEvents.SERVER_STOPPED.register((s2) -> {
                try {
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            ServerLifecycleEvents.SERVER_STARTED.register((s) -> {
                Fantasy.get(s).openTemporaryWorld(new RuntimeWorldConfig().setGenerator(new VoidChunkGenerator(s.getRegistryManager().get(Registry.BIOME_KEY).getEntry(0).get())));
                Fantasy.get(s).getOrOpenPersistentWorld(
                        new Identifier("fantasytest:test"),
                        new RuntimeWorldConfig()
                                .setGenerator(s.getOverworld().getChunkManager().getChunkGenerator())
                );
                System.out.println("External saves dir: " + storage.getSavesDirectory().toAbsolutePath());
                Fantasy.get(s).getOrOpenPersistentWorld(
                        new Identifier("fantasytest:test2"),
                        new RuntimeWorldConfig()
                                .setDimensionType(DimensionTypes.OVERWORLD)
                                .setGenerator(new VoidChunkGenerator(s.getRegistryManager().get(Registry.BIOME_KEY))),
                        session
                );
            });
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
                dispatcher.register(CommandManager.literal("unload-dim").executes(context -> {
                    Fantasy.get(context.getSource().getServer()).getOrOpenPersistentWorld(new Identifier("fantasytest:test2"), null).unload();
                    return Command.SINGLE_SUCCESS;
                }));
                dispatcher.register(CommandManager.literal("load-dim").executes(context -> {
                    try {
                        Fantasy.get(context.getSource().getServer()).getOrOpenPersistentWorld(
                                new Identifier("fantasytest:test2"),
                                new RuntimeWorldConfig()
                                        .setDimensionType(DimensionTypes.OVERWORLD)
                                        .setGenerator(new VoidChunkGenerator(context.getSource().getServer().getRegistryManager().get(Registry.BIOME_KEY))),
                                session
                        );
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    return Command.SINGLE_SUCCESS;
                }));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
