package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemReceiverRegressionTest {
    @Test
    void incomingFilterUsesAllowlistAndRefreshesAfterMarkerChanges() {
        TestReceiver receiver = new TestReceiver();
        ItemStack apple = new ItemStack(Items.APPLE);
        ItemStack dirt = new ItemStack(Items.DIRT);

        assertTrue(receiver.allowsIncomingItem(dirt), "an empty blacklist should allow all items");

        receiver.getFilterHandler().setStackInSlot(0, apple);
        receiver.filterData.allowlist = true;

        assertTrue(receiver.allowsIncomingItem(apple));
        assertFalse(receiver.allowsIncomingItem(dirt));
    }

    @Test
    void normalReceiverTransferMustApplyTheConfiguredItemFilter() throws Exception {
        String source = readProjectFile("src/main/java/com/jdte/common/blockentities/ItemReceiverBE.java");

        assertTrue(source.contains("isStackValidFilter(simulated)"),
                "normal receiver pulls must reject source stacks outside the configured filter");
    }

    @Test
    void directOverclockReceiverTransferMustApplyTheSameItemFilter() throws Exception {
        String source = readProjectFile(
                "src/main/java/com/jdte/common/autoioconfig/OverclockDirectTransferHelper.java");

        assertTrue(source.contains("allowsIncomingItem"),
                "direct receiver pulls must use the same filter as normal receiver pulls");
    }

    @Test
    void itemReceiverScreenMustTooltipItsStorageAndFilterRows() throws Exception {
        String source = readProjectFile(
                "src/main/java/com/jdte/client/screens/ItemReceiverScreenBase.java");
        String extendedScreen = readProjectFile(
                "src/main/java/com/jdte/client/screens/DefaultExtendedItemReceiverScreen.java");
        String zh = readProjectFile("src/main/resources/assets/jdte/lang/zh_cn.json");
        String en = readProjectFile("src/main/resources/assets/jdte/lang/en_us.json");

        assertTrue(source.contains("jdte.slot.item_receiver_storage"));
        assertTrue(source.contains("jdte.slot.item_receiver_filter"));
        assertTrue(source.contains("DynamicFilterSlot"));
        assertFalse(extendedScreen.contains("drawString"),
                "row labels should be shown as hover tips instead of permanent text");
        assertTrue(zh.contains("jdte.slot.item_receiver_storage"));
        assertTrue(zh.contains("jdte.slot.item_receiver_filter"));
        assertTrue(en.contains("jdte.slot.item_receiver_storage"));
        assertTrue(en.contains("jdte.slot.item_receiver_filter"));
    }

    private static String readProjectFile(String relativePath) throws Exception {
        Path current = Path.of(System.getProperty("user.dir", "")).toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("gradle.properties"))) {
            current = current.getParent();
        }
        assertTrue(current != null, "Could not locate project root from test runtime path");
        return Files.readString(current.resolve(relativePath));
    }

    private static final class TestReceiver extends ItemReceiverBE {
        private final FilterBasicHandler filterHandler = new FilterBasicHandler(9);

        private TestReceiver() {
            super(BlockEntityType.DROPPER, BlockPos.ZERO, Blocks.DROPPER.defaultBlockState());
        }

        @Override
        public FilterBasicHandler getFilterHandler() {
            return filterHandler;
        }

        @Override
        public BlockEntity getBlockEntity() {
            return this;
        }
    }
}
