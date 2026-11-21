package com.jdte.common.content;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public record ContentSelection(Identifier id) {
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

    public boolean matches(Identifier candidate) {
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

    private static Identifier normalize(String rawId) {
        String trimmed = Objects.requireNonNull(rawId, "rawId").strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Content id must not be blank");
        }
        if (trimmed.indexOf(':') < 0) {
            trimmed = "jdte:" + trimmed;
        }
        Identifier parsed = Identifier.tryParse(trimmed);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid content id: " + rawId);
        }
        return parsed;
    }
}
