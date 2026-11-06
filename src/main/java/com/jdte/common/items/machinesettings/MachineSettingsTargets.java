package com.jdte.common.items.machinesettings;
import com.jdte.common.blockentities.LargeMineralExtractorPartBE;
import com.jdte.common.blocks.LargeMineralExtractorPartBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
/** Both copier tiers address a large mineral machine through any of its parts. */
public final class MachineSettingsTargets {
    private MachineSettingsTargets() { }
    public static BlockEntity resolve(Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof LargeMineralExtractorPartBE && level.getBlockState(pos).getBlock() instanceof LargeMineralExtractorPartBlock part)
            return part.getController(level,pos,level.getBlockState(pos));
        return entity;
    }
}
