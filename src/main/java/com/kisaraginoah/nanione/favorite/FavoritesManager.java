package com.kisaraginoah.nanione.favorite;

import net.minecraft.world.item.ItemStack;

import java.util.*;

public class FavoritesManager {

    private static final List<ItemStack> FAVORITES = new ArrayList<>();

    public static boolean toggleFavorite(ItemStack stack) {
        if (stack.isEmpty()) return false;

        ItemStack copy = stack.copy();

        for (int i = 0; i < FAVORITES.size(); i++) {
            ItemStack fav = FAVORITES.get(i);
            if (ItemStack.isSameItemSameComponents(fav, copy)) {
                FAVORITES.remove(i);
                return false;
            }
        }

        FAVORITES.add(copy);
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
}
