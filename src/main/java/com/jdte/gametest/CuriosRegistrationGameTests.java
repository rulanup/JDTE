package com.jdte.gametest;
import com.jdte.setup.JDTEItems;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
@PrefixGameTestTemplate(false)
public final class CuriosRegistrationGameTests {
 @GameTest(templateNamespace="jdte",template="mineral_empty") public static void tankUsesOptionalCuriosRegistrationWithoutChangingFluidCapability(GameTestHelper h){
  var stack=new ItemStack(JDTEItems.BIG_FLUID_TANK.get());
  h.assertTrue(top.theillusivec4.curios.api.CuriosApi.getCurio(stack).isPresent(),"optional registration supplies Curios behavior");
  h.assertTrue(stack.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent(),"native tank capability remains");h.succeed();
 }
}
