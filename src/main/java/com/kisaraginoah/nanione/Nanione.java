package com.kisaraginoah.nanione;

import com.kisaraginoah.nanione.module.favorite.FavoritesCreativeTabs;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(value = Nanione.MODID, dist = Dist.CLIENT)
public class Nanione {

    public static final String MODID = "nanione";

    public static final Logger LOGGER = LogUtils.getLogger();

    public Nanione(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        FavoritesCreativeTabs.CREATIVE_TABS.register(modEventBus);
    }
}
