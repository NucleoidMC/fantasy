package xyz.nucleoid.fantasy.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.fantasy.Fantasy;

import java.nio.file.Path;

@Mixin(SavedDataStorage.class)
public class SavedDataStorageMixin {
    @WrapOperation(method = "readTagFromDisk", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag dontDataFix(DataFixTypes instance, DataFixer fixer, CompoundTag tag, int fromVersion, int toVersion, Operation<CompoundTag> original, @Local(name = "dataFile") Path dataFile) {
        if (dataFile.getFileName().toString().equals("world_clocks.dat") && dataFile.getParent().getFileName().toString().equals(Fantasy.ID)) {
            return tag;
        } else {
            return original.call(instance, fixer, tag, fromVersion, toVersion);
        }
    }
}
