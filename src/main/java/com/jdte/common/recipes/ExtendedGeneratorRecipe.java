package com.jdte.common.recipes;

import com.google.gson.JsonObject;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Same shaped ingredients as source, preserving packed machine state instead of deleting it. */
public final class ExtendedGeneratorRecipe extends ShapedRecipe {
    private final ShapedRecipe delegate;
    public ExtendedGeneratorRecipe(ShapedRecipe delegate) { super(delegate.getId(),delegate.getGroup(),delegate.category(),delegate.getWidth(),delegate.getHeight(),delegate.getIngredients(),delegate.getResultItem(RegistryAccess.EMPTY),delegate.showNotification());this.delegate=delegate; }
    private ItemStack source(CraftingContainer input) {
        for(int i=0;i<input.getContainerSize();i++) {
            var stack=input.getItem(i);
            if(stack.is(com.jdte.setup.JDTEItems.ADVANCED_ENTITY_ACCELERATOR.get()) || stack.is(com.jdte.setup.JDTEItems.ADVANCED_TIME_ACCELERATOR.get()) || stack.is(com.jdte.setup.JDTEItems.ADVANCED_LIFE_EXTRACTOR.get()) || stack.is(com.jdte.setup.JDTEItems.ADVANCED_BIO_EXTRACTOR.get()) || stack.is(com.direwolf20.justdirethings.setup.Registration.GeneratorT1.get().asItem())
                    || stack.is(com.direwolf20.justdirethings.setup.Registration.GeneratorFluidT1.get().asItem()))return stack;
        }
        return ItemStack.EMPTY;
    }
    @Override public boolean matches(CraftingContainer input,Level level) {
        if(!delegate.matches(input,level))return false;
        // Occupied inactive destination storage cannot be silently overwritten.
        var caps=(source(input).hasTag()?source(input).getTag():new CompoundTag()).getCompound("JustDiresBEData").getCompound("ForgeCaps").getCompound("jdte:machine_data");
        var old=source(input);var data=old.hasTag()?old.getTag().getCompound("JustDiresBEData"):new CompoundTag();
        if(old.is(com.jdte.setup.JDTEItems.ADVANCED_LIFE_EXTRACTOR.get()) && data.getDouble("pendingLifeFluid")!=0)return false;
        if (!caps.getCompound("extended_upgrade_handler").getList("Items",Tag.TAG_COMPOUND).isEmpty())return false;
        var result=delegate.getResultItem(level.registryAccess());
        if(result.is(com.jdte.setup.JDTEItems.ADVANCED_ENTITY_ACCELERATOR.get()) || result.is(com.jdte.setup.JDTEItems.EXTENDED_ENTITY_ACCELERATOR.get()))return validEntityTransfer(data,caps,result);
        return true;
    }
    private static boolean validEntityTransfer(CompoundTag data,CompoundTag caps,ItemStack result) {
        boolean extended=result.is(com.jdte.setup.JDTEItems.EXTENDED_ENTITY_ACCELERATOR.get());
        var block=extended?com.jdte.setup.JDTEBlocks.EXTENDED_ENTITY_ACCELERATOR.get():com.jdte.setup.JDTEBlocks.ADVANCED_ENTITY_ACCELERATOR.get();
        var target=extended?new com.jdte.common.blockentities.ExtendedEntityAcceleratorBE(net.minecraft.core.BlockPos.ZERO,block.defaultBlockState()):new com.jdte.common.blockentities.AdvancedEntityAcceleratorBE(net.minecraft.core.BlockPos.ZERO,block.defaultBlockState());
        var handler=extended?new com.jdte.common.upgrades.ExtendedUpgradeItemStackHandler(target){@Override protected void onContentsChanged(int slot){}}:new com.jdte.common.upgrades.UpgradeItemStackHandler(target){@Override protected void onContentsChanged(int slot){}};
        var encoded=caps.getCompound("upgrade_handler");
        for(var raw:encoded.getList("Items",Tag.TAG_COMPOUND)) {
            var entry=(CompoundTag)raw;int slot=entry.getByte("Slot")&255;var stack=ItemStack.of(entry);
            if(slot>=4 || stack.getCount()!=1 || !handler.isItemValid(slot,stack))return false;
            handler.setStackInSlot(slot,stack);
        }
        int capacity=count(handler,com.jdte.common.upgrades.UpgradeType.CAPACITY);
        int fluid=capacity+count(handler,com.jdte.common.upgrades.UpgradeType.FLUID);
        boolean ultimate=count(handler,com.jdte.common.upgrades.UpgradeType.ULTIMATE_CAPACITY)>0;
        long energyLimit=Math.min(Integer.MAX_VALUE,(long)target.baseEnergyCapacity()*(ultimate?50:1)*(1L<<capacity));
        long fluidLimit=Math.min(Integer.MAX_VALUE,(long)target.baseFluidCapacity()*(ultimate?50:1)*(1L<<fluid));
        var stored=net.minecraftforge.fluids.FluidStack.loadFluidStackFromNBT(data.getCompound("fluidTank"));
        double pending=data.getDouble("pendingFluidCost");
        return data.getInt("energy")>=0 && data.getInt("energy")<=energyLimit && stored.getAmount()<=fluidLimit
                && (stored.isEmpty() || stored.getFluid() instanceof com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid)
                && Double.isFinite(pending) && pending>=0 && pending<1;
    }
    private static int count(com.jdte.common.upgrades.UpgradeItemStackHandler handler,com.jdte.common.upgrades.UpgradeType type){int total=0;for(int i=0;i<handler.getSlots();i++)if(com.jdte.common.upgrades.UpgradeHelper.isUpgrade(handler.getStackInSlot(i),type))total++;return total;}
    @Override public ItemStack assemble(CraftingContainer input,RegistryAccess access) {
        var result=delegate.assemble(input,access);var old=source(input);
        if(old.hasTag()) {
            result.setTag(old.getTag().copy());
            if(result.getTag().contains("JustDiresBEData",Tag.TAG_COMPOUND)) {
                var data=result.getTag().getCompound("JustDiresBEData");
                var all=data.getCompound("ForgeCaps");var caps=all.getCompound("jdte:machine_data");
                if(!result.is(com.jdte.setup.JDTEItems.ADVANCED_BIO_EXTRACTOR.get()) && !result.is(com.jdte.setup.JDTEItems.ADVANCED_ENTITY_ACCELERATOR.get())) {
                    var expanded=caps.getCompound("upgrade_handler").copy();expanded.putInt("Size",8);
                    caps.put("extended_upgrade_handler",expanded);
                    var empty=new CompoundTag();empty.putInt("Size",4);caps.put("upgrade_handler",empty);
                }
                all.put("jdte:machine_data",caps);data.put("ForgeCaps",all);
                data.putString("id",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(result.getItem()).toString());
            }
        }
        return result;
    }
    @Override public boolean canCraftInDimensions(int w,int h) { return delegate.canCraftInDimensions(w,h); }
    @Override public ItemStack getResultItem(RegistryAccess access) { return delegate.getResultItem(access); }
    @Override public ResourceLocation getId() { return delegate.getId(); }
    @Override public RecipeSerializer<?> getSerializer() { return JDTERecipes.EXTENDED_GENERATOR_RECIPE_SERIALIZER.get(); }
    @Override public CraftingBookCategory category() { return delegate.category(); }
    @Override public String getGroup() { return delegate.getGroup(); }
    @Override public NonNullList<Ingredient> getIngredients() { return delegate.getIngredients(); }
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) { return NonNullList.withSize(input.getContainerSize(),ItemStack.EMPTY); }
    public static final class Serializer implements RecipeSerializer<ExtendedGeneratorRecipe> {
        @Override public ExtendedGeneratorRecipe fromJson(ResourceLocation id,JsonObject json) { return new ExtendedGeneratorRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(id,json)); }
        @Override public ExtendedGeneratorRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf buf) { return new ExtendedGeneratorRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(id,buf)); }
        @Override public void toNetwork(FriendlyByteBuf buf,ExtendedGeneratorRecipe recipe) { RecipeSerializer.SHAPED_RECIPE.toNetwork(buf,recipe.delegate); }
    }
}
