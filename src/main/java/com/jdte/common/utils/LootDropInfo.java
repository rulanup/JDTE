package com.jdte.common.utils;

import net.minecraft.resources.Identifier;

public record LootDropInfo(Identifier itemId, int minCount, int maxCount, String chanceLabel) { }
