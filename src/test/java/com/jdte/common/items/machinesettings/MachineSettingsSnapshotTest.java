package com.jdte.common.items.machinesettings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MachineSettingsSnapshotTest {
 private CompoundTag valid(){var root=new CompoundTag();var custom=new CompoundTag();custom.putInt("multiplier",17);MachineSettingsSnapshot.write(root,new ResourceLocation("jdte:mineral_extractor"),1,2,custom);return root;}
 @Test void roundTripPreservesTypeAndCustomSettings(){var decoded=MachineSettingsSnapshot.read(valid()).orElseThrow();assertEquals("jdte:mineral_extractor",decoded.machineType().toString());assertEquals(17,decoded.custom().getInt("multiplier"));assertEquals(1,decoded.tickSpeed());assertEquals(2,decoded.direction());}
 @Test void unknownSchemaIsRejected(){var root=valid();root.getCompound("jdteMachineSettings").putInt("schemaVersion",2);assertTrue(MachineSettingsSnapshot.read(root).isEmpty());}
 @Test void wrongNbtTypesAreRejected(){var root=valid();root.getCompound("jdteMachineSettings").putString("tickSpeed","1");assertTrue(MachineSettingsSnapshot.read(root).isEmpty());}
 @Test void invalidDirectionAndDelayAreRejected(){for(int direction:new int[]{-1,6,Integer.MAX_VALUE}){var root=valid();root.getCompound("jdteMachineSettings").putInt("direction",direction);assertTrue(MachineSettingsSnapshot.read(root).isEmpty());}var root=valid();root.getCompound("jdteMachineSettings").putInt("tickSpeed",0);assertTrue(MachineSettingsSnapshot.read(root).isEmpty());}
 @Test void invalidResourceIdentifierIsRejected(){var root=valid();root.getCompound("jdteMachineSettings").putString("machineType","INVALID ID");assertTrue(MachineSettingsSnapshot.read(root).isEmpty());}
 @Test void decodedCustomSettingsDoNotAliasSavedNbt(){var root=valid();var decoded=MachineSettingsSnapshot.read(root).orElseThrow();decoded.custom().putInt("multiplier",100);assertEquals(17,MachineSettingsSnapshot.read(root).orElseThrow().custom().getInt("multiplier"));}
 @Test void strictBooleanRejectsInvalidBytesAndIntegers(){var tag=new CompoundTag();tag.putInt("value",1);assertTrue(MachineSettingsCodecSupport.readBoolean(tag,"value").isEmpty());tag.putByte("value",(byte)2);assertTrue(MachineSettingsCodecSupport.readBoolean(tag,"value").isEmpty());tag.putBoolean("value",true);assertEquals(true,MachineSettingsCodecSupport.readBoolean(tag,"value").orElseThrow());}
}
