package com.jdte.common.blockentities;

import com.mojang.authlib.GameProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LootFabricatorLootingProfileTest {
    @Test
    void lootRollsShareAStableFakePlayerFactoryCacheKey() {
        GameProfile first = LootFabricatorBE.createLootingProfile();
        GameProfile second = LootFabricatorBE.createLootingProfile();

        assertEquals("[JDTE Loot Fabricator]", first.getName());
        assertEquals("[JDTE Loot Fabricator]", second.getName());
        assertNotNull(first.getId());
        assertEquals(first, second);
        assertEquals(first.getId(), second.getId());
    }
}
