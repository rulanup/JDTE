package com.jdte.datagen;
import com.jdte.JDTE;
import com.jdte.setup.JDTEItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
public class JDTEFumoItemModelProvider extends ItemModelProvider {
    public JDTEFumoItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, JDTE.MODID, existingFileHelper);
    }
    @Override
    protected void registerModels() {
        withExistingParent(JDTEItems.XINGYE_FUMO.getId().toString(), modLoc("block/xingye_fumo"));
        withExistingParent(JDTEItems.CAINIAOYO_FUMO.getId().toString(), modLoc("block/cainiaoyo_fumo"));
        withExistingParent(JDTEItems.MAIQILIN_FUMO.getId().toString(), modLoc("block/maiqilin_fumo"));
    }
}