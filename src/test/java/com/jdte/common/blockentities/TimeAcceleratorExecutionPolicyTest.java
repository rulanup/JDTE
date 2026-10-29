package com.jdte.common.blockentities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TimeAcceleratorExecutionPolicyTest {
 @Test void sourceGlobalBudgetSwitch(){assertEquals(4096,TimeAcceleratorExecutionPolicy.globalBudget(false,4096));assertEquals(Long.MAX_VALUE,TimeAcceleratorExecutionPolicy.globalBudget(true,1));assertEquals(0,TimeAcceleratorExecutionPolicy.globalBudget(false,-1));}
 @Test void requestAdmitsOnlyRemainingBatchBudget(){assertEquals(64,TimeAcceleratorExecutionPolicy.requestedTicks(32768,64,4096));assertEquals(5,TimeAcceleratorExecutionPolicy.requestedTicks(32768,64,5));assertEquals(0,TimeAcceleratorExecutionPolicy.requestedTicks(100,64,0));}
 @Test void overflowSafeCapacity(){assertFalse(TimeAcceleratorExecutionPolicy.canAdmitFullWork(Integer.MAX_VALUE,1024,Long.MAX_VALUE));assertTrue(TimeAcceleratorExecutionPolicy.canAdmitFullWork(16,1024,1008));assertFalse(TimeAcceleratorExecutionPolicy.canAdmitFullWork(17,1024,1008));}
 @Test void sourceWandCanAdmitPartialAggregateCredit(){assertEquals(24,TimeAcceleratorExecutionPolicy.admittedWandWorkTicks(32768,1024,1000));assertEquals(0,TimeAcceleratorExecutionPolicy.admittedWandWorkTicks(2,1024,1024));assertEquals(32768,TimeAcceleratorExecutionPolicy.admittedWandWorkTicks(32768,1000000,0));}
}
