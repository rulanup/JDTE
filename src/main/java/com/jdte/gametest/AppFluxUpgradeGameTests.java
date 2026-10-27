package com.jdte.gametest;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.blockentity.storage.DriveBlockEntity;
import com.jdte.common.integrations.ae2.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.List;
import static com.jdte.gametest.AEUpgradeGameTests.*;
@PrefixGameTestTemplate(false)
public final class AppFluxUpgradeGameTests {
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=240) public static void extractionRefillsFeFromActualAppliedFluxCell(GameTestHelper h){
        grid(h);var drive=(DriveBlockEntity)h.getBlockEntity(DRIVE);
        drive.getInternalInventory().insertItem(2,new ItemStack(com.glodblock.github.appflux.common.AFItemAndBlock.FE_CELL_1k),false);
        h.runAfterDelay(80,()->{
            var storage=ap(h).getGrid().getStorageService().getInventory();var action=IActionSource.empty();
            var key=com.glodblock.github.appflux.common.me.key.FluxKey.of(com.glodblock.github.appflux.common.me.key.type.EnergyType.FE);
            var equipment=new ItemStack(com.direwolf20.justdirethings.setup.Registration.EclipseAlloyPickaxe.get());var energy=equipment.getCapability(ForgeCapabilities.ENERGY).orElseThrow(()->new IllegalStateException("energy"));
            int before=energy.getEnergyStored(),budget=Math.min(50000,energy.getMaxEnergyStored()-before);h.assertTrue(budget>0,"equipment has demand");
            h.assertTrue(storage.insert(key,budget,Actionable.MODULATE,action)==budget,"seed real Applied Flux FE cell");
            AEWirelessLinkData.set(equipment,target(h));AEWirelessLinkData.setExtractionEnabled(equipment);
            AEExtractionNetwork.refill(net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel()),List.of(equipment));
            h.assertTrue(energy.getEnergyStored()==before+budget,"FE arrives exactly once");h.assertTrue(storage.extract(key,budget,Actionable.SIMULATE,action)==0,"network FE conserved");h.succeed();
        });
    }
}
