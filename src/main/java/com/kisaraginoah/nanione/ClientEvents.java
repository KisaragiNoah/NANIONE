package com.kisaraginoah.nanione;

import com.kisaraginoah.nanione.mixin.CreativeModeInventoryScreenAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        if (event.getScreen() instanceof CreativeModeInventoryScreen creativeModeInventoryScreen) {
            EditBox box = ((CreativeModeInventoryScreenAccessor) creativeModeInventoryScreen).nanione$getSearchBox();
            if (box.isFocused()) return;
        }

        KeyMapping keyMapping = FavoriteKeyMappings.TOGGLE_FAVORITE.get();
        InputConstants.Key inputKey = InputConstants.getKey(event.getKeyEvent());

        if (!keyMapping.isActiveAndMatches(inputKey)) return;

        Slot slot = screen.getSlotUnderMouse();
        if (slot == null) return;
        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return;

        boolean added = FavoritesManager.toggleFavorite(stack);

        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            Component title;
            Component desc;

            if (added) {
                title = Component.translatable("nanione.toast.favorite_added.title");
                desc = Component.translatable("nanione.toast.favorite_added.desc", stack.getHoverName());
            } else {
                title = Component.translatable("nanione.toast.favorite_removed.title");
                desc = Component.translatable("nanione.toast.favorite_removed.desc", stack.getHoverName());
            }

            mc.getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, title, desc));
        }

        if (mc.screen instanceof CreativeModeInventoryScreen creativeModeInventoryScreen && creativeModeInventoryScreen instanceof FavoritesTabRefresher refresher) {
            refresher.nanione$refreshFavoritesTab();
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (FavoriteKeyMappings.OPEN_FAVORITES_TAB.get().consumeClick()) {

            if (!mc.gameMode.getPlayerMode().isCreative()) continue;

            if (mc.screen instanceof CreativeModeInventoryScreen creativeModeInventoryScreen && creativeModeInventoryScreen instanceof FavoritesTabSelector selector) {
                selector.nanione$selectFavoritesTab();
                continue;
            }

            CreativeModeInventoryScreen screen = new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), mc.player.canUseGameMasterBlocks());
            mc.setScreen(screen);

            if (screen instanceof FavoritesTabSelector selector) {
                selector.nanione$selectFavoritesTab();
            }
        }
    }
}
