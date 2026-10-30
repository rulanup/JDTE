package com.jdte.common.blockentities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ExperienceStorageMathTest {
 @Test void wholeXpFluidBoundariesAndSimulationPlans(){
  assertEquals(0,ExperienceStorageMath.fillAmount(0,19));assertEquals(20,ExperienceStorageMath.fillAmount(0,39));
  assertEquals(2147483640,ExperienceStorageMath.fillAmount(0,Integer.MAX_VALUE));
  assertEquals(0,ExperienceStorageMath.fillAmount(107374182,1000));
  assertEquals(0,ExperienceStorageMath.fillAmount(Integer.MAX_VALUE,Integer.MAX_VALUE));
  assertEquals(Integer.MAX_VALUE,ExperienceStorageMath.fluidAmount(Integer.MAX_VALUE));
  assertEquals(2147483640,ExperienceStorageMath.drainAmount(Integer.MAX_VALUE,Integer.MAX_VALUE));
 }
 @Test void noNegativeOrOverflowOperations(){
  assertEquals(0,ExperienceStorageMath.acceptedXp(9,-1));assertEquals(1,ExperienceStorageMath.acceptedXp(Integer.MAX_VALUE-1,Integer.MAX_VALUE));
  assertEquals(0,ExperienceStorageMath.fillAmount(10,-1));assertEquals(0,ExperienceStorageMath.drainAmount(10,-1));
  assertEquals(0,ExperienceStorageMath.fluidAmount(-1));assertEquals(0,ExperienceStorageMath.drainAmount(-1,100));
 }
 @Test void mergedOrbsKeepUnacceptedMultiplicity(){
  assertEquals(3,ExperienceStorageMath.acceptedOrbCount(0,7,3));
  assertEquals(1,ExperienceStorageMath.acceptedOrbCount(Integer.MAX_VALUE-10,7,3));
  assertEquals(0,ExperienceStorageMath.acceptedOrbCount(Integer.MAX_VALUE-6,7,3));
  assertEquals(0,ExperienceStorageMath.acceptedOrbCount(0,0,3));
  assertEquals(1,ExperienceStorageMath.acceptedOrbCount(0,Integer.MAX_VALUE,3));
 }
}
