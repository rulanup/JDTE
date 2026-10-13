package com.jdte.client;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/** 将旧版物品材质包释放到游戏的资源包目录。 */
final class LegacyTexturePackExporter {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final String PACK_RESOURCE = "/META-INF/jdte/jdte_legacy_textures.zip";
    private static final String PACK_FILE_NAME = "JDTE-Legacy-Textures.zip";

    private LegacyTexturePackExporter() {
    }

    static void exportIfMissing(Path gameDirectory, Supplier<InputStream> packResource) {
        Path destination = gameDirectory.resolve("resourcepacks").resolve(PACK_FILE_NAME);
        if (Files.exists(destination)) {
            return;
        }

        try {
            Files.createDirectories(destination.getParent());
            try (InputStream input = packResource.get()) {
                if (input == null) {
                    LOGGER.warn("Could not find the bundled JDTE legacy texture pack.");
                    return;
                }
                try {
                    Files.copy(input, destination);
                } catch (FileAlreadyExistsException ignored) {
                    // Keep a pack that appeared after the initial existence check.
                }
            }
        } catch (IOException exception) {
            LOGGER.warn("Could not export the JDTE legacy texture pack to {}.", destination, exception);
        }
    }
}
