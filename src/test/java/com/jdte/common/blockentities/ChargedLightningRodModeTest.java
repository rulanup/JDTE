package com.jdte.common.blockentities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ChargedLightningRodModeTest {
 @Test void sourceRatesAndCosts(){assertEquals(10000,ChargedLightningRodMode.NORMAL.energyPerStrike);assertEquals(200,ChargedLightningRodMode.NORMAL.intervalTicks);assertEquals(200000,ChargedLightningRodMode.OVERCLOCK.energyPerStrike*ChargedLightningRodMode.OVERCLOCK.strikesPerCycle);assertEquals(5000000,ChargedLightningRodMode.ULTIMATE.energyPerStrike*ChargedLightningRodMode.ULTIMATE.strikesPerCycle);}
 @Test void budgetsNeverReserveIncompleteStrike(){for(var mode:ChargedLightningRodMode.values()){assertEquals(0,mode.affordableStrikes(-1));assertEquals(0,mode.affordableStrikes(mode.energyPerStrike-1));assertEquals(1,mode.affordableStrikes(mode.energyPerStrike));assertEquals(mode.strikesPerCycle,mode.affordableStrikes(Integer.MAX_VALUE));}}
 @Test void onlyNormalModeWaitsForCooldown(){assertFalse(ChargedLightningRodMode.NORMAL.canBegin(1));assertTrue(ChargedLightningRodMode.NORMAL.canBegin(0));assertTrue(ChargedLightningRodMode.OVERCLOCK.canBegin(200));assertTrue(ChargedLightningRodMode.ULTIMATE.canBegin(200));}
}
