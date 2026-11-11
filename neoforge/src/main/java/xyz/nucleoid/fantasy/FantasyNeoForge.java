package xyz.nucleoid.fantasy;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import xyz.nucleoid.fantasy.util.TransientChunkGenerator;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

@Mod(Fantasy.ID)
public final class FantasyNeoForge {
    public FantasyNeoForge(IEventBus modBus) {
        modBus.addListener(FantasyNeoForge::register);

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> Fantasy.onServerTickStart(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> Fantasy.onServerStopping(event.getServer()));
    }

    private static void register(RegisterEvent event) {
        event.register(Registries.CHUNK_GENERATOR, helper -> {
            helper.register(Identifier.fromNamespaceAndPath(Fantasy.ID, "void"), VoidChunkGenerator.CODEC);
            helper.register(Identifier.fromNamespaceAndPath(Fantasy.ID, "transient"), TransientChunkGenerator.CODEC);
        });
    }
}
