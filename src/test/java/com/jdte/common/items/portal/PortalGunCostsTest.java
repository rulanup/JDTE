package com.jdte.common.items.portal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PortalGunCostsTest {
    @Test void crossDimensionAlwaysCostsFiveHundred() {assertEquals(500,PortalGunCosts.manual(false,0));assertEquals(500,PortalGunCosts.manual(false,30_000_000));}
    @Test void sameDimensionCostsTwentyFivePerBlock() {assertEquals(250,PortalGunCosts.manual(true,10));}
    @Test void fractionalDistanceRoundsUp() {assertEquals(26,PortalGunCosts.manual(true,1.001));}
    @Test void distanceChargeStopsAtTwentyFiveThousand() {assertEquals(25000,PortalGunCosts.manual(true,1000));assertEquals(25000,PortalGunCosts.manual(true,30_000_000));}
    @Test void samePositionNeedsNoFluid() {assertEquals(0,PortalGunCosts.manual(true,0));}
    @Test void hugeDistancesCannotOverflowToNegativePrices() {assertEquals(25000,PortalGunCosts.manual(true,Double.MAX_VALUE));}
    @Test void manualSlotDoesNotInvokeJdtFavoritePricing() {assertEquals(25,PortalGunCosts.destination(false,true,true,1,()->{throw new AssertionError("manual slots use their own rate");}));}
    @Test void previousAndRecordedDestinationsUseJdtEvenWithManualSlotSelected() {assertEquals(777,PortalGunCosts.destination(true,true,false,300,()->777));assertEquals(888,PortalGunCosts.destination(false,false,true,300,()->888));}
}
