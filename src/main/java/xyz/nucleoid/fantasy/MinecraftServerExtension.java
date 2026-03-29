package xyz.nucleoid.fantasy;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface MinecraftServerExtension {
    RuntimeServerClockManager fantasy$clockManager();
}
