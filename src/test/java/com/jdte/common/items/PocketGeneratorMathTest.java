package com.jdte.common.items;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PocketGeneratorMathTest {
    @Test void sourceCapacityAndCoalBudget() { assertEquals(4_000_000,LargePortableContainerLogic.pocketGeneratorCapacity(1_000_000)); assertEquals(400,PocketGeneratorMath.burnTicks(1600,4)); assertEquals(24000,400*PocketGeneratorMath.multiply(15,4)); }
    @Test void multiplicationNeverWrapsNegative() { assertEquals(Integer.MAX_VALUE,PocketGeneratorMath.multiply(Integer.MAX_VALUE,1000)); assertEquals(0,PocketGeneratorMath.multiply(-1,Integer.MAX_VALUE)); assertEquals(Integer.MAX_VALUE,LargePortableContainerLogic.pocketGeneratorCapacity(Integer.MAX_VALUE)); }
    @Test void shortFuelAndRemaindersUseWholeSourceTicks() { assertEquals(0,PocketGeneratorMath.burnTicks(100,1000)); assertEquals(3,PocketGeneratorMath.burnTicks(101,30)); assertEquals(0,PocketGeneratorMath.burnTicks(-1,4)); assertEquals(0,PocketGeneratorMath.burnTicks(100,0)); }
    @Test void guiAndNbtBoundsUseWideArithmetic() { assertEquals(70,PocketGeneratorMath.pixels(Integer.MAX_VALUE,Integer.MAX_VALUE,70)); assertEquals(35,PocketGeneratorMath.pixels(500,1000,70)); assertEquals(0,PocketGeneratorMath.pixels(-1,10,13)); assertEquals(10,PocketGeneratorMath.bounded(Integer.MAX_VALUE,10)); assertEquals(0,PocketGeneratorMath.bounded(-1,100)); }
}
