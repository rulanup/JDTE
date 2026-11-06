package com.jdte.common.acceleration;

import com.jdte.common.acceleration.ExternalTimeAccelerationBackend;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AEAccelerationFrameTest {
    @Test void preparesAllContributorsBeforeExecutingAndClearsAfterBackend(){
        var b=new Backend();ExternalTimeAccelerationFrame.run(null,b,List.of(new ExternalTimeAccelerationFrame.Work(()->b.log.add("prepare1"),()->b.log.add("ordinary1")),new ExternalTimeAccelerationFrame.Work(()->b.log.add("prepare2"),()->b.log.add("ordinary2"))));
        assertEquals(List.of("begin","prepare1","prepare2","ordinary1","ordinary2","execute","clear"),b.log);
    }
    @Test void failedPreparationClearsOpenFrameAndPropagatesFailure(){
        var b=new Backend();var failure=new IllegalStateException("prepare");assertSame(failure,assertThrows(IllegalStateException.class,()->ExternalTimeAccelerationFrame.run(null,b,List.of(new ExternalTimeAccelerationFrame.Work(()->{throw failure;},()->fail("ordinary must not run"))))));assertEquals(List.of("begin","clear"),b.log);
    }
    @Test void failedBackendExecutionClearsOpenFrameAndPropagatesFailure(){
        var b=new Backend();b.failExecute=true;assertThrows(IllegalStateException.class,()->ExternalTimeAccelerationFrame.run(null,b,List.of()));assertEquals(List.of("begin","execute","clear"),b.log);
    }
    @Test void failedBeginDoesNotClearAnUnopenedFrame(){
        var b=new Backend();b.failBegin=true;assertThrows(IllegalStateException.class,()->ExternalTimeAccelerationFrame.run(null,b,List.of()));assertEquals(List.of("begin"),b.log);
    }
    private static final class Backend implements ExternalTimeAccelerationBackend {
        final List<String> log=new ArrayList<>();boolean failBegin,failExecute;
        public void beginFrame(MinecraftServer s){log.add("begin");if(failBegin)throw new IllegalStateException("begin");}
        public TargetResolution resolve(ServerLevel l,BlockPos p){return TargetResolution.notExternal();}
        public void submit(TargetHandle h,Object c,int n){}
        public void executeFrame(MinecraftServer s){log.add("execute");if(failExecute)throw new IllegalStateException("execute");}
        public void clearFrame(MinecraftServer s){log.add("clear");}
    }
}
