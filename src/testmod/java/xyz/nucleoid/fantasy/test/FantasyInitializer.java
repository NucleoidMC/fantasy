package xyz.nucleoid.fantasy.test;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.TeleportTarget;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.util.HashMap;
import java.util.WeakHashMap;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class FantasyInitializer implements ModInitializer {
    private HashMap<Identifier, RuntimeWorldHandle> worlds = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((s) -> {
            Fantasy.get(s).openTemporaryWorld(
                    new RuntimeWorldConfig().setGenerator(new VoidChunkGenerator(s.getRegistryManager().get(RegistryKeys.BIOME).getEntry(0).get())).setWorldConstructor(CustomWorld::new)
            );

            Fantasy.get(s).openTemporaryWorld(
                    new RuntimeWorldConfig().setGenerator(s.getOverworld().getChunkManager().getChunkGenerator()).setWorldConstructor(CustomWorld::new)
            );
        });

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("fantasy_open").then(
                    argument("name", IdentifierArgumentType.identifier())
                            .executes((context -> {
                                try {
                                    var t = System.currentTimeMillis();
                                    var id = IdentifierArgumentType.getIdentifier(context, "name");

                                    var x = Fantasy.get(context.getSource().getServer()).getOrOpenPersistentWorld(
                                            id,
                                            new RuntimeWorldConfig()
                                                    .setGenerator(context.getSource().getServer().getOverworld().getChunkManager().getChunkGenerator())
                                                    .setSeed(id.hashCode())
                                    );
                                    {
                                        var text = Text.literal("WorldCreate: " + (System.currentTimeMillis() - t));
                                        context.getSource().sendFeedback(() -> text, false);
                                    }
                                    worlds.put(id, x);

                                    t = System.currentTimeMillis();
                                    FabricDimensions.teleport(context.getSource().getEntity(), x.asWorld(), new TeleportTarget(new Vec3d(0, 100 ,0) , Vec3d.ZERO, 0, 0));
                                    {
                                        var text = Text.literal("Teleport: " + (System.currentTimeMillis() - t));
                                        context.getSource().sendFeedback(() -> text, false);
                                    }                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }

                                return 0;
                            }))
            ));

            dispatcher.register(literal("fantasy_delete").then(
                    argument("name", IdentifierArgumentType.identifier())
                            .executes((context -> {
                                try {
                                    var id = IdentifierArgumentType.getIdentifier(context, "name");
                                    worlds.get(id).delete();
                                    worlds.remove(id);
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                                return 0;
                            }))
            ));

            dispatcher.register(literal("fantasy_unload").then(
                    argument("name", IdentifierArgumentType.identifier())
                            .executes((context -> {
                                try {
                                    var id = IdentifierArgumentType.getIdentifier(context, "name");
                                    worlds.get(id).unload();
                                    worlds.remove(id);
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }

                                return 0;
                            }))
            ));
        }));
    }
}
