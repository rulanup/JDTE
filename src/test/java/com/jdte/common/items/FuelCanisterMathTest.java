package com.jdte.common.items;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FuelCanisterMathTest {
 @Test void sourceFuelScales(){assertEquals(40000000,LargePortableContainerLogic.fuelCapacity(10000000));assertEquals(2000,LargePortableContainerLogic.fuelMinimumConsumption(200));assertEquals(80,LargePortableContainerLogic.fuelBurnMultiplier(8));}
 @Test void scalesSaturate(){assertEquals(Integer.MAX_VALUE,LargePortableContainerLogic.fuelCapacity(Integer.MAX_VALUE));assertEquals(Integer.MAX_VALUE,LargePortableContainerLogic.fuelMinimumConsumption(Integer.MAX_VALUE));assertEquals(Integer.MAX_VALUE,LargePortableContainerLogic.fuelBurnMultiplier(Integer.MAX_VALUE));}
 @Test void wholeItemsOnly(){assertEquals(0,FuelCanisterMath.acceptedItems(39999999,40000000,1600,64));assertEquals(1,FuelCanisterMath.acceptedItems(39998400,40000000,1600,64));}
 @Test void offeredBound(){assertEquals(64,FuelCanisterMath.acceptedItems(0,40000000,1600,64));assertEquals(1,FuelCanisterMath.acceptedItems(0,Integer.MAX_VALUE,Integer.MAX_VALUE,64));}
 @Test void invalidInputs(){assertEquals(0,FuelCanisterMath.acceptedItems(-1,100,1,1));assertEquals(0,FuelCanisterMath.acceptedItems(101,100,1,1));assertEquals(0,FuelCanisterMath.acceptedItems(0,100,0,1));assertEquals(0,FuelCanisterMath.acceptedItems(0,100,1,-1));}
 @Test void upgradeBudget(){assertEquals(240000,FuelCanisterMath.energyBudget(2000,40,3));assertEquals(0,FuelCanisterMath.energyBudget(Integer.MAX_VALUE,40,3));assertEquals(0,FuelCanisterMath.energyBudget(1600,0,3));}
 @Test void counterCannotOverflow(){assertEquals(Integer.MAX_VALUE-1,FuelCanisterMath.safeBurnCounter(Integer.MAX_VALUE,1));assertEquals(20,FuelCanisterMath.safeBurnCounter(2000,100));}
}
