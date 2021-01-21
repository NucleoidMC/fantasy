package xyz.nucleoid.fantasy.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.fantasy.util.ScreenHandlerAccess;

import java.util.List;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin implements ScreenHandlerAccess {
    @Shadow
    @Final
    public List<Slot> slots;
    @Shadow
    @Final
    private DefaultedList<ItemStack> trackedStacks;

    @Override
    public void resetTrackedState() {
        for (int i = 0; i < this.slots.size(); i++) {
            ItemStack stack = this.slots.get(i).getStack();
            this.trackedStacks.set(i, stack);
        }
    }
}
