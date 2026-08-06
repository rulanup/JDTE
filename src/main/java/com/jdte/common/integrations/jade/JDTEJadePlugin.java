package com.jdte.common.integrations.jade;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.JDTE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import java.util.ArrayList;
import java.util.List;

@WailaPlugin(JDTE.MODID)
public class JDTEJadePlugin implements IWailaPlugin {
    private static final ResourceLocation UID = JDTE.id("installed_upgrades");
    private static final String TAG_UPGRADES = "jdte_upgrades";
    private static final UpgradeProvider UPGRADE_PROVIDER = new UpgradeProvider();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(UPGRADE_PROVIDER, BaseMachineBE.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(UPGRADE_PROVIDER, BaseMachineBlock.class);
    }

    private static class UpgradeProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof BaseMachineBE machine)) {
                return;
            }

            List<ItemStack> upgrades = new ArrayList<>();
            collect(upgrades, UpgradeHelper.getUpgradeHandler(machine));
            if (machine instanceof BioCrusherBE crusher) {
                collect(upgrades, crusher.getLootingHandler());
                collect(upgrades, crusher.getSharpnessHandler());
            }
            if (upgrades.isEmpty()) {
                return;
            }

            ListTag serialized = new ListTag();
            for (ItemStack upgrade : upgrades) {
                serialized.add(upgrade.save(accessor.getLevel().registryAccess()));
            }
            data.put(TAG_UPGRADES, serialized);
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            ListTag serialized = accessor.getServerData().getList(TAG_UPGRADES, Tag.TAG_COMPOUND);
            if (serialized.isEmpty()) {
                return;
            }

            tooltip.add(Component.translatable("jade.jdte.installed_upgrades").withStyle(ChatFormatting.GRAY));
            IElementHelper elements = IElementHelper.get();
            for (int index = 0; index < serialized.size(); index++) {
                ItemStack upgrade = ItemStack.parseOptional(
                        accessor.getLevel().registryAccess(), serialized.getCompound(index));
                if (upgrade.isEmpty()) {
                    continue;
                }
                Component label = Component.literal(" ")
                        .append(upgrade.getHoverName())
                        .append(Component.literal(" x" + upgrade.getCount()).withStyle(ChatFormatting.GRAY));
                tooltip.add(List.of(elements.smallItem(upgrade.copyWithCount(1)), elements.text(label)));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        private static void collect(List<ItemStack> upgrades, IItemHandler handler) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemStack existing = upgrades.stream()
                        .filter(candidate -> ItemStack.isSameItemSameComponents(candidate, stack))
                        .findFirst()
                        .orElse(null);
                if (existing == null) {
                    upgrades.add(stack.copy());
                } else {
                    existing.grow(stack.getCount());
                }
            }
        }
    }
}
