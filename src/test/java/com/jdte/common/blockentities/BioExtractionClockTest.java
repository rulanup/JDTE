package com.jdte.common.blockentities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BioExtractionClockTest {
 @Test void ordinaryBoundary(){var n=BioExtractionClock.advance(19,1,20);assertEquals(1,n.cycles());assertEquals(0,n.remaining());}
 @Test void partialCycle(){var n=BioExtractionClock.advance(2,1,5);assertEquals(0,n.cycles());assertEquals(3,n.remaining());}
 @Test void acceleratedBatchIsBounded(){var n=BioExtractionClock.advance(19,2000000,20);assertEquals(64,n.cycles());assertEquals(19,n.remaining());}
 @Test void wideSumCannotWrap(){var n=BioExtractionClock.advance(Integer.MAX_VALUE,Integer.MAX_VALUE,20);assertEquals(64,n.cycles());assertEquals(14,n.remaining());}
 @Test void defensivePeriodAndCounters(){var n=BioExtractionClock.advance(-7,-9,0);assertEquals(0,n.cycles());assertEquals(0,n.remaining());}
 @Test void underclockRemainder(){var n=BioExtractionClock.advance(39,83,40);assertEquals(3,n.cycles());assertEquals(2,n.remaining());}
}
