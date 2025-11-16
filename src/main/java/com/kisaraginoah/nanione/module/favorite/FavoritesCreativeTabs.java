package com.kisaraginoah.nanione.module.favorite;

import com.kisaraginoah.nanione.Nanione;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FavoritesCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Nanione.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FAVORITES_TAB =
            CREATIVE_TABS.register("favorites", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Nanione.MODID + ".favorites"))
                    .icon(() -> new ItemStack(Items.NETHER_STAR))
                    .displayItems((params, output) -> output.accept(Items.DIAMOND)).build());
}
