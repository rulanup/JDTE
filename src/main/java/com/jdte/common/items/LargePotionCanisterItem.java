package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.jdte.common.containers.LargePotionCanisterMenuAccess;
import com.jdte.common.containers.handlers.LargePotionCanisterHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import java.util.List;

public class LargePotionCanisterItem extends PotionCanister {
    public static int getPotionCapacityMb(){return PotionCanisterMath.CAPACITY;}
    public int getCapacityMb(){return getPotionCapacityMb();}
    public static int getFullness(ItemStack stack){return PotionCanisterMath.fullness(LargePotionCanisterData.amount(stack));}
    public static boolean tryFillBatch(ItemStack canister,ItemStack input){return LargePotionCanisterData.tryFillBatch(canister,input);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){
        int amount=LargePotionCanisterData.amount(stack);
        if(amount>0&&LargePotionCanisterData.validContents(LargePotionCanisterData.contents(stack))){
            tooltip.add(Component.literal(MagicHelpers.formatted(amount)+"/"+MagicHelpers.formatted(getPotionCapacityMb())));
            PotionUtils.addPotionTooltip(LargePotionCanisterData.asPotion(LargePotionCanisterData.contents(stack)),tooltip,1f);
        }
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        if(!level.isClientSide)LargePotionCanisterMenuAccess.open(player,false);
        return new InteractionResultHolder<>(InteractionResult.PASS,player.getItemInHand(hand));
    }
    @Override public ICapabilityProvider initCapabilities(ItemStack stack,CompoundTag nbt){
        return new ICapabilityProvider(){
            private final LazyOptional<net.minecraftforge.items.IItemHandler> items=LazyOptional.of(()->new LargePotionCanisterHandler(stack,true));
            public <T> LazyOptional<T> getCapability(Capability<T> cap,Direction side){return cap==ForgeCapabilities.ITEM_HANDLER?items.cast():LazyOptional.empty();}
        };
    }
}
