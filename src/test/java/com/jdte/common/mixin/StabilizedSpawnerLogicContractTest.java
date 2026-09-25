package com.jdte.common.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 龙之进化稳定刷怪笼 mixin 契约。{@code @Accessor} 按方法描述符精确匹配目标成员，
 * getter 的返回类型必须与 DE 私有 tile 字段类型（TileStabilizedSpawner）完全一致——
 * 曾因返回 Object（按 tile:Ljava/lang/Object; 查找无候选）导致 StabilizedSpawnerLogic
 * 在放置刷怪笼加载类时 InvalidAccessorException 崩溃。嵌套拦截类也必须在
 * mixins.jdte.json 中显式注册，否则粉碎逻辑永远不会触发。
 *
 * <p>DE 是可选依赖且无法进入单测类路径（FML JUnit 引导会按 mod 链解析它），
 * 因此这里直接断言编译产物类文件字节码中的方法描述符。</p>
 */
class StabilizedSpawnerLogicContractTest {

    /** DE 私有字段 tile 的确切类型描述符（对 DE 3.1.4.632/633 逐字节核对）。 */
    private static final String TILE_FIELD_TYPE =
        "Lcom/brandon3055/draconicevolution/blocks/tileentity/TileStabilizedSpawner;";

    @Test
    void tileAccessorReturnTypeMatchesDraconicFieldType() throws IOException {
        byte[] accessorClass = readClassBytes("com/jdte/mixin/StabilizedSpawnerLogicAccessor.class");

        assertTrue(contains(accessorClass, "jdte$tile"),
            "访问器缺少 jdte$tile 方法");
        assertTrue(contains(accessorClass, "()" + TILE_FIELD_TYPE),
            "jdte$tile 的返回描述符必须是 ()" + TILE_FIELD_TYPE
                + "；返回 Object 会导致 StabilizedSpawnerLogic 加载即 InvalidAccessorException 崩溃");
        assertFalse(contains(accessorClass, "()Ljava/lang/Object;"),
            "jdte$tile 不得返回 Object：@Accessor 按返回类型描述符查找字段，将找不到 tile 候选");
    }

    @Test
    void draconicSpawnerMixinIsRegistered() throws IOException {
        JsonObject config = readMixinConfig();

        assertTrue(config.getAsJsonArray("mixins").asList().stream()
                .anyMatch(element -> "SpawnerMixin$DraconicSpawnerMixin".equals(element.getAsString())),
            "SpawnerMixin$DraconicSpawnerMixin 必须注册在 mixins.jdte.json，否则 DE 粉碎拦截不生效");
    }

    private static JsonObject readMixinConfig() throws IOException {
        try (InputStream stream = StabilizedSpawnerLogicContractTest.class.getClassLoader()
                .getResourceAsStream("mixins.jdte.json")) {
            assertNotNull(stream);
            JsonObject config = JsonParser.parseString(
                    new String(stream.readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
            assertEquals("com.jdte.mixin", config.get("package").getAsString());
            return config;
        }
    }

    private static byte[] readClassBytes(String path) throws IOException {
        try (InputStream stream = StabilizedSpawnerLogicContractTest.class.getClassLoader()
                .getResourceAsStream(path)) {
            assertNotNull(stream, () -> path + " 不在测试类路径上");
            return stream.readAllBytes();
        }
    }

    private static boolean contains(byte[] bytes, String ascii) {
        byte[] needle = ascii.getBytes(StandardCharsets.US_ASCII);
        search:
        for (int i = 0; i <= bytes.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (bytes[i + j] != needle[j]) {
                    continue search;
                }
            }
            return true;
        }
        return false;
    }
}
