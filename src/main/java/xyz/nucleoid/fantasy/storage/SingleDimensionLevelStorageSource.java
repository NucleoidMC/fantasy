package xyz.nucleoid.fantasy.storage;

import com.mojang.datafixers.DataFixer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SingleDimensionLevelStorageSource extends LevelStorageSource {
    private final DirectoryValidator worldDirValidator;
    private final Path worldPath;

    public SingleDimensionLevelStorageSource(Path path, DirectoryValidator directoryValidator, DataFixer dataFixer) {
        super(path.getParent(), null, directoryValidator, dataFixer);
        this.worldDirValidator = directoryValidator;
        this.worldPath = path;
    }

    @Override
    public LevelStorageSource.@NonNull LevelStorageAccess validateAndCreateAccess(@NonNull String name) throws IOException, ContentValidationException {
        List<ForbiddenSymlinkInfo> list = this.worldDirValidator.validateDirectory(this.worldPath, true);
        if (!list.isEmpty()) {
            throw new ContentValidationException(this.worldPath, list);
        } else {
            return new LevelStorageAccess(name, this.worldPath);
        }
    }

    @Override
    public LevelStorageSource.@NonNull LevelStorageAccess createAccess(@NonNull String name) throws IOException {
        return new LevelStorageAccess(name, this.worldPath);
    }

    class LevelStorageAccess extends LevelStorageSource.LevelStorageAccess {
        public LevelStorageAccess(String name, Path path) throws IOException {
            super(name, path);
        }

        @Override
        @NonNull
        public Path getDimensionPath(@NonNull ResourceKey<Level> resourceKey) {
            return SingleDimensionLevelStorageSource.this.worldPath;
        }

        @Override
        @NonNull
        public LevelStorageSource parent() {
            return SingleDimensionLevelStorageSource.this;
        }

        @Override
        public long makeWorldBackup() {
            return 0;
        }
    }
}
