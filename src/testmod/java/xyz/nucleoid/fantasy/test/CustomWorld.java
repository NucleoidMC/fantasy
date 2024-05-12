package xyz.nucleoid.fantasy.test;

import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.RuntimeWorld;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.util.function.BooleanSupplier;

public class CustomWorld extends RuntimeWorld {
    private long dynSeed;
    private final RecipeManager recipeManager;

    protected CustomWorld(MinecraftServer server, RegistryKey<World> registryKey, RuntimeWorldConfig config, Style style) {
        super(server, registryKey, config, style);
        this.recipeManager = new RecipeManager(server.getRegistryManager());
    }


    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
        this.dynSeed = this.random.nextLong();
        super.tick(shouldKeepTicking);
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    @Override
    public long getSeed() {
        return this.dynSeed;
    }
}
