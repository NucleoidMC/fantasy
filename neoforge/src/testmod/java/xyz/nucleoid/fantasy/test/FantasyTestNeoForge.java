package xyz.nucleoid.fantasy.test;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("fantasy_test")
public final class FantasyTestNeoForge {
    private final FantasyTest test = new FantasyTest();

    public FantasyTestNeoForge() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> this.test.onServerStarted(event.getServer()));
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> this.test.registerCommands(event.getDispatcher()));
    }
}
