package com.jdte.gametest;

import com.jdte.common.factory.FactoryPackageStorage;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.gametest.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class FactoryPackageConcurrencyGameTests {
    @GameTest(template="mineral_empty",timeoutTicks=300)
    public static void concurrentPackageIoUsesCapturedWorldPath(GameTestHelper h) {
        // Minecraft's LevelStorageAccess path cache is not thread safe.
        var root=h.getLevel().getServer().getWorldPath(LevelResource.ROOT);
        var registries=h.getLevel().registryAccess();
        var dimension=h.getLevel().dimension().location();
        List<CompletableFuture<Void>> tasks=new ArrayList<>();
        for(int i=0;i<8;i++) {
            var id=UUID.randomUUID();var token=UUID.randomUUID();
            var data=new FactoryPackageStorage.PackageData(2,id,dimension,BlockPos.ZERO,new Vec3i(1,1,1),List.of(),List.of(),List.of());
            tasks.add(CompletableFuture.runAsync(()->{
                try {
                    FactoryPackageStorage.write(root,data,1_000_000);
                    FactoryPackageStorage.claim(root,id,token);
                    var restored=FactoryPackageStorage.read(root,id,token,1_000_000,registries);
                    if(!restored.id().equals(id))throw new IllegalStateException("Package identity changed");
                    FactoryPackageStorage.release(root,id,token);
                    FactoryPackageStorage.deleteRegular(root,id);
                } catch(java.io.IOException error) { throw new java.util.concurrent.CompletionException(error); }
            },net.minecraft.Util.ioPool()));
        }
        var all=CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
        h.succeedWhen(()->{h.assertTrue(all.isDone(),"wait for parallel package I/O");all.join();});
    }
}
