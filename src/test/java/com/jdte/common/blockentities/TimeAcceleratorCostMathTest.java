package com.jdte.common.blockentities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TimeAcceleratorCostMathTest {
 @Test void tierAndWorkScaling(){assertEquals(.01,TimeAcceleratorCostMath.fluidCost(3,1,1,2),1e-12);assertEquals(.025,TimeAcceleratorCostMath.fluidCost(3,1,1,5),1e-12);assertEquals(300,TimeAcceleratorCostMath.energyCost(3,100));}
 @Test void fractionalConservation(){double pending=0;int paid=0;for(int i=0;i<125;i++){var result=TimeAcceleratorCostMath.settleFluid(pending,.01);paid+=result.drainMb();pending=result.remainingCost();}assertEquals(1,paid);assertEquals(.25,pending,1e-9);}
 @Test void boundaryAndEmpty(){var paid=TimeAcceleratorCostMath.settleFluid(.75,.25);assertEquals(1,paid.drainMb());assertEquals(0,paid.remainingCost());assertEquals(0,TimeAcceleratorCostMath.energyCost(-1,100));assertEquals(0,TimeAcceleratorCostMath.fluidCost(0,1,1,5));}
 @Test void energySaturatesRatherThanWraps(){assertEquals(Integer.MAX_VALUE,TimeAcceleratorCostMath.energyCost(Integer.MAX_VALUE,Integer.MAX_VALUE));assertEquals(0,TimeAcceleratorCostMath.energyCost(100,-1));}
}
