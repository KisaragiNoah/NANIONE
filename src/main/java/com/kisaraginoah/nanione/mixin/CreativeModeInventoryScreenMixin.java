package com.kisaraginoah.nanione.mixin;

import com.kisaraginoah.nanione.Nanione;
import com.kisaraginoah.nanione.favorite.FavoritesManager;
import com.kisaraginoah.nanione.favorite.FavoritesTabRefresher;
import com.kisaraginoah.nanione.favorite.FavoritesTabSelector;
import com.kisaraginoah.nanione.init.ModCreativeTabs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin implements FavoritesTabRefresher, FavoritesTabSelector {

    @Shadow
    private float scrollOffs;

    @Shadow
    private static CreativeModeTab selectedTab;

    @Shadow
    private void selectTab(CreativeModeTab tab) {}

    @Unique
    private static final ResourceLocation FAVORITE_STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nanione.MODID, "textures/gui/favorite_star.png");

    @Unique
    private void nanione$rebuildFavorites(boolean resetScroll) {
        if (selectedTab != ModCreativeTabs.FAVORITES_TAB.get()) return;

        AbstractContainerMenu rawMenu = ((AbstractContainerScreenAccessor) this).nanione$getMenu();

        CreativeModeInventoryScreen.ItemPickerMenu pickerMenu = (CreativeModeInventoryScreen.ItemPickerMenu) rawMenu;

        NonNullList<ItemStack> items = pickerMenu.items;

        items.clear();
        for (ItemStack fav : FavoritesManager.getFavoritesStacks()) {
            ItemStack display = fav.copy();
            display.setCount(1);
            items.add(display);
        }

        if (resetScroll) {
            this.scrollOffs = 0.0F;
        }
        pickerMenu.scrollTo(this.scrollOffs);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void nanione$renderFavoriteStarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AbstractContainerMenu rawMenu = ((AbstractContainerScreenAccessor) this).nanione$getMenu();

        for (Slot slot : rawMenu.slots) {
            if (!slot.hasItem()) continue;

            ItemStack stack = slot.getItem();
            if (!FavoritesManager.isFavorite(stack)) continue;

            int x = ((AbstractContainerScreenAccessor) this).nanione$getLeftPos() + slot.x;
            int y = ((AbstractContainerScreenAccessor) this).nanione$getTopPos() + slot.y;
            int x1 = x + 8;
            int y1 = y + 8;

            guiGraphics.blit(FAVORITE_STAR_TEXTURE, x, y, x1, y1, 0.0F, 1.0F, 0.0F, 1.0F);
        }
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void nanione$onSelectTab(CreativeModeTab tab, CallbackInfo ci) {
        nanione$rebuildFavorites(true);
    }

    @Override
    public void nanione$refreshFavoritesTab() {
        nanione$rebuildFavorites(false);
    }

    @Override
    public void nanione$selectFavoritesTab() {
        this.selectTab(ModCreativeTabs.FAVORITES_TAB.get());
    }
}