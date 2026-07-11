package com.jdte.common.utils;

import net.minecraft.resources.ResourceLocation;

public record LootDropInfo(ResourceLocation itemId, int minCount, int maxCount, String chanceLabel) { }
