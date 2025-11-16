package com.kisaraginoah.nanione.favorite;

import com.kisaraginoah.nanione.Nanione;
import com.mojang.serialization.DataResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class FavoritesManager {

    private static final List<ItemStack> FAVORITES = new ArrayList<>();
    private static final Pattern INVALID_PATH = Pattern.compile("[^a-zA-Z0-9-_.]");

    private static Path favoritesRoot;
    private static String activeContextKey;
    private static HolderLookup.Provider activeRegistryAccess;

    public static void handleClientTick(Minecraft mc) {
        if (favoritesRoot == null) {
            favoritesRoot = mc.gameDirectory.toPath().resolve("nanione").resolve("favorites");
        }

        if (mc.level != null) {
            activeRegistryAccess = mc.level.registryAccess();
        }

        String newContext = determineContextKey(mc);
        if (Objects.equals(newContext, activeContextKey)) return;

        saveCurrentFavorites();
        activeContextKey = newContext;
        loadFavoritesForContext();
    }

    private static String determineContextKey(Minecraft mc) {
        if (mc.player == null || mc.level == null) return null;

        if (mc.hasSingleplayerServer()) {
            IntegratedServer server = mc.getSingleplayerServer();
            if (server != null) {
                String levelName = server.getWorldData().getLevelName();
                return "world/" + sanitize(levelName);
            }
        }

        ServerData serverData = mc.getCurrentServer();
        if (serverData != null) return "server/" + sanitize(serverData.ip);

        if (mc.getConnection() != null) {
            String fallback = mc.getConnection().getConnection().getRemoteAddress().toString();
            return "server/" + sanitize(fallback);
        }

        return null;
    }

    private static String sanitize(String value) {
        String sanitized = INVALID_PATH.matcher(value).replaceAll("_");
        return sanitized.isEmpty() ? "default" : sanitized;
    }

    public static boolean toggleFavorite(ItemStack stack) {
        if (stack.isEmpty()) return false;

        ItemStack copy = stack.copy();

        for (int i = 0; i < FAVORITES.size(); i++) {
            ItemStack fav = FAVORITES.get(i);
            if (ItemStack.isSameItemSameComponents(fav, copy)) {
                FAVORITES.remove(i);
                saveCurrentFavorites();
                return false;
            }
        }

        FAVORITES.add(copy);
        saveCurrentFavorites();
        return true;
    }

    public static boolean isFavorite(ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (ItemStack fav : FAVORITES) {
            if (ItemStack.isSameItemSameComponents(fav, stack)) {
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> getFavoritesStacks() {
        return FAVORITES.stream().map(ItemStack::copy).toList();
    }

    private static void saveCurrentFavorites() {
        if (activeContextKey == null || favoritesRoot == null) return;

        RegistryOps<Tag> ops = createRegistryOps();
        if (ops == null) return;

        try {
            Path file = favoritesRoot.resolve(activeContextKey + ".nbt");
            Files.createDirectories(file.getParent());

            ListTag listTag = new ListTag();
            for (ItemStack stack : FAVORITES) {
                DataResult<Tag> encoded = ItemStack.CODEC.encodeStart(ops, stack);
                encoded.resultOrPartial(error -> Nanione.LOGGER.error("Failed to encode favorite in context {}: {}", activeContextKey, error)).ifPresent(listTag::add);
            }

            CompoundTag rootTag = new CompoundTag();
            rootTag.put("items", listTag);

            NbtIo.write(rootTag, file);
        } catch (IOException exception) {
            Nanione.LOGGER.error("Failed to save favorites for context {}", activeContextKey, exception);
        }
    }

    private static void loadFavoritesForContext() {
        FAVORITES.clear();

        if (activeContextKey == null || favoritesRoot == null) return;

        RegistryOps<Tag> ops = createRegistryOps();
        if (ops == null) return;

        Path file = favoritesRoot.resolve(activeContextKey + ".nbt");
        if (!Files.exists(file)) return;

        try {
            CompoundTag rootTag = NbtIo.read(file);
            ListTag list = rootTag.getListOrEmpty("items");
            for (Tag entry : list) {
                ItemStack.CODEC.parse(ops, entry).resultOrPartial(error -> Nanione.LOGGER.error("Failed to decode favorite int context {}: {}", activeContextKey, error)).ifPresent(stack -> {
                    if (!stack.isEmpty()) {
                        FAVORITES.add(stack);
                    }
                });
            }
        } catch (IOException exception) {
            Nanione.LOGGER.error("Failed to load favorites for context {}", activeContextKey, exception);
        }
    }

    private static RegistryOps<Tag> createRegistryOps() {
        if (activeRegistryAccess == null) return null;

        return RegistryOps.create(NbtOps.INSTANCE, activeRegistryAccess);
    }
}
