package com.kisaraginoah.nanione.favorite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FavoriteToast implements Toast {

    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/recipe");

    private static final long DISPLAY_TIME = 3000L;

    private final ItemStack icon;
    private final Component title;
    private final Component desc;

    private long lastChanged;
    private boolean changed = true;
    private Visibility wantedVisibility = Visibility.HIDE;

    public FavoriteToast(ItemStack icon, Component title, Component desc) {
        this.icon = icon.copy();
        this.icon.setCount(1);
        this.title = title;
        this.desc = desc;
    }

    @Override
    public @NotNull Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(@NotNull ToastManager toastManager, long visibilityTime) {
        if (this.changed) {
            this.lastChanged = visibilityTime;
            this.changed = false;
        }

        double duration = DISPLAY_TIME * toastManager.getNotificationDisplayTimeMultiplier();
        long elapsed = visibilityTime - this.lastChanged;

        this.wantedVisibility = elapsed < duration ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public void render(GuiGraphics guiGraphics, @NotNull Font font, long visibilityTime) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        guiGraphics.drawString(font, this.title, 30, 7, 0xFF_FFFF00, false);
        guiGraphics.drawString(font, this.desc, 30, 18, 0xFF_FFFFFF, false);

        guiGraphics.renderFakeItem(this.icon, 8, 8);
    }

    @Override
    public int width() {
        Minecraft mc = Minecraft.getInstance();
        int wTitle = mc.font.width(this.title);
        int wDesc = mc.font.width(this.desc);
        return Math.max(160, 30 + Math.max(wTitle, wDesc));
    }

    @Override
    public int height() {
        return Toast.super.height();
    }
}
