package com.jdte.common.blockentities;

import com.mojang.authlib.GameProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class LootFabricatorLootingProfileTest {
    @Test
    void eachLootRollGetsAnIsolatedFakePlayerProfile() {
        GameProfile first = LootFabricatorBE.createLootingProfile();
        GameProfile second = LootFabricatorBE.createLootingProfile();

        assertEquals("[JDTE Loot Fabricator]", first.getName());
        assertEquals("[JDTE Loot Fabricator]", second.getName());
        assertNotSame(first, second);
        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotEquals(first.getId(), second.getId());
    }
}
