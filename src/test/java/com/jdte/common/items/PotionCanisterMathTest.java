package com.jdte.common.items;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PotionCanisterMathTest {
 @Test void sourceBatchAndCapacity(){assertEquals(4000,PotionCanisterMath.CAPACITY);assertEquals(1000,PotionCanisterMath.BATCH);assertEquals(4,PotionCanisterMath.POTIONS_PER_BATCH);assertTrue(PotionCanisterMath.canFill(3000,4));assertFalse(PotionCanisterMath.canFill(3001,4));assertFalse(PotionCanisterMath.canFill(0,3));}
 @Test void invalidAndOverflow(){assertFalse(PotionCanisterMath.canFill(-1,4));assertFalse(PotionCanisterMath.canFill(Integer.MAX_VALUE,4));assertEquals(0,PotionCanisterMath.boundedAmount(Long.MIN_VALUE));assertEquals(4000,PotionCanisterMath.boundedAmount(Long.MAX_VALUE));}
 @Test void sourceFullnessSteps(){assertEquals(0,PotionCanisterMath.fullness(0));assertEquals(1,PotionCanisterMath.fullness(1));assertEquals(1,PotionCanisterMath.fullness(1000));assertEquals(2,PotionCanisterMath.fullness(1001));assertEquals(4,PotionCanisterMath.fullness(Integer.MAX_VALUE));}
}
