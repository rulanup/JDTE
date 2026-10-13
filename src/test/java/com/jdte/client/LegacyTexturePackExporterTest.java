package com.jdte.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyTexturePackExporterTest {
    @TempDir
    Path gameDirectory;

    @Test
    void exportsBundledPackIntoResourcepacksWhenMissing() throws IOException {
        byte[] packedResource = {1, 2, 3, 4};
        Path destination = gameDirectory.resolve("resourcepacks/JDTE-Legacy-Textures.zip");

        LegacyTexturePackExporter.exportIfMissing(gameDirectory,
                () -> new ByteArrayInputStream(packedResource));

        assertTrue(Files.isRegularFile(destination));
        assertArrayEquals(packedResource, Files.readAllBytes(destination));
    }

    @Test
    void preservesAnExistingPackWithoutReadingBundledResource() throws IOException {
        Path destination = gameDirectory.resolve("resourcepacks/JDTE-Legacy-Textures.zip");
        Files.createDirectories(destination.getParent());
        byte[] userPack = {9, 8, 7};
        Files.write(destination, userPack);
        AtomicBoolean openedBundledResource = new AtomicBoolean();

        LegacyTexturePackExporter.exportIfMissing(gameDirectory, () -> {
            openedBundledResource.set(true);
            return new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        });

        assertFalse(openedBundledResource.get());
        assertArrayEquals(userPack, Files.readAllBytes(destination));
    }
}
