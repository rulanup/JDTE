package com.jdte.common.content;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record ContentSelection(ResourceLocation id) {
    public ContentSelection {
        id = Objects.requireNonNull(id, "id");
    }

    public static ContentSelection parse(String rawId) {
        return new ContentSelection(normalize(rawId));
    }

    public static boolean isValid(String rawId) {
        try {
            normalize(rawId);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public boolean matches(ResourceLocation candidate) {
        if (candidate == null) {
            return false;
        }
        if (!id.getNamespace().equals(candidate.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        String candidatePath = candidate.getPath();
        return candidatePath.equals(path) || candidatePath.startsWith(path + "/");
    }

    private static ResourceLocation normalize(String rawId) {
        String trimmed = Objects.requireNonNull(rawId, "rawId").strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Content id must not be blank");
        }
        if (trimmed.indexOf(':') < 0) {
            trimmed = "jdte:" + trimmed;
        }
        ResourceLocation parsed = ResourceLocation.tryParse(trimmed);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid content id: " + rawId);
        }
        return parsed;
    }
}
