package com.jdte.common.recipes;

import com.google.gson.*;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import java.util.*;

/** Forge datapack and bounded network form of the pinned entity-harvest recipe. */
public record BioExtractorRecipe(ResourceLocation entityType,Optional<FluidStack> outputFluid,List<BioFactoryOutput> outputItems,
                                 int energy,int cooldown,float damage,ResourceLocation id) implements Recipe<CraftingContainer> {
    public BioExtractorRecipe {
        if(energy<0||cooldown<0||!Float.isFinite(damage)||damage<0||outputItems.size()>64)throw new IllegalArgumentException("Invalid Bio Extractor recipe "+id);
        outputFluid=outputFluid.map(FluidStack::copy);outputItems=List.copyOf(outputItems);
        if(outputFluid.isPresent()&&outputFluid.get().isEmpty())throw new IllegalArgumentException("Empty Bio Extractor fluid "+id);
        for(var out:outputItems)if(out.stack().isEmpty()||!Float.isFinite(out.chance())||out.chance()<0||out.chance()>1)throw new IllegalArgumentException("Invalid Bio Extractor item/chance "+id);
    }
    public boolean matches(EntityType<?> type){return entityType.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type));}
    @Override public boolean matches(CraftingContainer input,Level level){return false;}
    @Override public ItemStack assemble(CraftingContainer input,RegistryAccess access){return getResultItem(access);}
    @Override public ItemStack getResultItem(RegistryAccess access){return outputItems.isEmpty()?ItemStack.EMPTY:outputItems.get(0).stack().copy();}
    @Override public boolean canCraftInDimensions(int width,int height){return false;}
    @Override public boolean isSpecial(){return true;}
    @Override public ResourceLocation getId(){return id;}
    @Override public RecipeType<?> getType(){return JDTERecipes.BIO_EXTRACTOR_RECIPE_TYPE.get();}
    @Override public RecipeSerializer<?> getSerializer(){return JDTERecipes.BIO_EXTRACTOR_RECIPE_SERIALIZER.get();}
    public static final class Serializer implements RecipeSerializer<BioExtractorRecipe> {
        @Override public BioExtractorRecipe fromJson(ResourceLocation id,JsonObject json) {
            var entity=new ResourceLocation(GsonHelper.getAsString(json,"entity"));
            Optional<FluidStack> fluid=Optional.empty();
            if(json.has("output_fluid")) {
                var out=GsonHelper.getAsJsonObject(json,"output_fluid");var key=new ResourceLocation(GsonHelper.getAsString(out,"id"));
                var type=key.equals(new ResourceLocation("minecraft","milk"))?net.minecraftforge.common.ForgeMod.MILK.get():BuiltInRegistries.FLUID.getOptional(key).orElseThrow(()->new JsonSyntaxException("Unknown Bio Extractor fluid "+key));
                fluid=Optional.of(new FluidStack(type,GsonHelper.getAsInt(out,"amount")));
            }
            List<BioFactoryOutput> items=new ArrayList<>();if(json.has("output_items"))for(var element:GsonHelper.getAsJsonArray(json,"output_items"))items.add(BioFactoryOutput.fromJson(element.getAsJsonObject()));
            return new BioExtractorRecipe(entity,fluid,items,GsonHelper.getAsInt(json,"energy",1000),GsonHelper.getAsInt(json,"cooldown",600),GsonHelper.getAsFloat(json,"damage",0),id);
        }
        @Override public BioExtractorRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf buf) {
            var entity=buf.readResourceLocation();Optional<FluidStack> fluid=buf.readBoolean()?Optional.of(buf.readFluidStack()):Optional.empty();
            int size=buf.readVarInt();if(size<0||size>64)throw new IllegalArgumentException("Invalid Bio Extractor output list size");List<BioFactoryOutput> items=new ArrayList<>();for(int i=0;i<size;i++)items.add(BioFactoryOutput.fromNetwork(buf));
            return new BioExtractorRecipe(entity,fluid,items,buf.readVarInt(),buf.readVarInt(),buf.readFloat(),id);
        }
        @Override public void toNetwork(FriendlyByteBuf buf,BioExtractorRecipe r) {buf.writeResourceLocation(r.entityType());buf.writeBoolean(r.outputFluid().isPresent());r.outputFluid().ifPresent(buf::writeFluidStack);buf.writeVarInt(r.outputItems().size());r.outputItems().forEach(o->o.toNetwork(buf));buf.writeVarInt(r.energy());buf.writeVarInt(r.cooldown());buf.writeFloat(r.damage());}
    }
}
