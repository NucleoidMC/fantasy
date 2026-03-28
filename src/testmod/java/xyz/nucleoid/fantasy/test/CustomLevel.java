package xyz.nucleoid.fantasy.test;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import xyz.nucleoid.fantasy.RuntimeLevel;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;

import java.util.function.BooleanSupplier;

public class CustomLevel extends RuntimeLevel {
    private long dynSeed;
    private final RecipeManager recipeManager;

    protected CustomLevel(MinecraftServer server, ResourceKey<Level> registryKey, RuntimeLevelConfig config, Style style) {
        super(server, registryKey, config, style);
        this.recipeManager = new RecipeManager(server.registryAccess());
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
        this.dynSeed = this.random.nextLong();
        super.tick(shouldKeepTicking);
    }

    @Override
    public RecipeManager recipeAccess() {
        return this.recipeManager;
    }

    @Override
    public long getSeed() {
        return this.dynSeed;
    }
}
