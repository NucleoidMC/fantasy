package xyz.nucleoid.fantasy.test;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class FantasyTestFabric implements ModInitializer {
    private final FantasyTest test = new FantasyTest();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this.test::onServerStarted);
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> this.test.registerCommands(dispatcher));
    }
}
