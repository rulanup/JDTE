package com.jdte.common.acceleration;

import net.minecraft.server.MinecraftServer;
import java.util.List;

/** Source frame ordering isolated from registry-backed world discovery for unit testing. */
public final class ExternalTimeAccelerationFrame {
    private ExternalTimeAccelerationFrame() {}
    public record Work(Runnable prepare, Runnable executeOrdinary) {}
    public static void run(MinecraftServer server, ExternalTimeAccelerationBackend backend, List<Work> work) {
        boolean open=false;
        try {
            if(backend!=null){backend.beginFrame(server);open=true;}
            for(var item:work)item.prepare().run();
            for(var item:work)item.executeOrdinary().run();
            if(backend!=null)backend.executeFrame(server);
        } finally { if(open)backend.clearFrame(server); }
    }
}
