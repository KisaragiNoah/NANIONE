package com.kisaraginoah.nanione.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Accessor("menu")
    AbstractContainerMenu nanione$getMenu();

    @Accessor("leftPos")
    int nanione$getLeftPos();

    @Accessor("topPos")
    int nanione$getTopPos();
}
