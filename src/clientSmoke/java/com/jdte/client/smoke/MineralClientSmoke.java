package com.jdte.client.smoke;

import com.jdte.client.MineralSurveyClientCache;
import com.jdte.client.screens.MineralExtractorScreen;
import com.jdte.client.screens.MineralSurveyScreen;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blocks.LargeMineralExtractorStructure;
import com.jdte.common.containers.MineralExtractorContainer;
import com.jdte.common.items.MineralSurveyItem;
import com.jdte.common.minerals.*;
import com.jdte.common.network.MineralPackets;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Files;
import java.util.List;

/** Opt-in real-client smoke checks. This source set is never packaged in the mod JAR. */
@Mod.EventBusSubscriber(modid = "jdte", value = Dist.CLIENT)
public final class MineralClientSmoke {
    private static int stage;
    private static boolean copierAreaBefore;
    private static int ticks;
    private static int changedAt;
    private static BlockPos normalPos;
    private static BlockPos largePos;
    private static BlockPos mixerPos;
    private static boolean mixerCursorPicked;
    private static MineralSurveyData survey;
    private static final org.slf4j.Logger LOG = com.mojang.logging.LogUtils.getLogger();
    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Mineral client smoke: " + message);
    }
    private static void next() { stage++; changedAt = ticks; }
    private static void screenshot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), msg -> LOG.info("Mineral smoke screenshot: {}", msg.getString()));
    }
    private static void aimAt(Minecraft mc, BlockPos pos) {
        var origin = mc.player.getEyePosition();
        var shape = mc.level.getBlockState(pos).getShape(mc.level, pos);
        for (var box : shape.toAabbs()) {
            var point = box.getCenter().add(pos.getX(), pos.getY(), pos.getZ());
            var hit = mc.level.clip(new net.minecraft.world.level.ClipContext(origin, point,
                    net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player));
            if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK && hit.getBlockPos().equals(pos)) {
                mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, point);
                return;
            }
        }
        throw new IllegalStateException("Mineral client smoke cannot see target " + pos + " from " + origin);
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Boolean.getBoolean("jdte.clientSmoke")) return;
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        if (ticks > 6000) throw new IllegalStateException("Mineral client smoke timed out at stage " + stage + ", screen=" + mc.screen);
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        int elapsed = ticks - changedAt;
        switch (stage) {
            case 0 -> {
                if (mc.player.tickCount < 100 || !mc.player.onGround()) return;
                normalPos = mc.player.blockPosition().offset(3, 1, 0);
                largePos = normalPos.offset(-6, 0, 0);
                var server = mc.getSingleplayerServer(); var id = mc.player.getUUID();
                server.execute(() -> {
                    var level = server.overworld(); var player = server.getPlayerList().getPlayer(id);
                    check(player != null, "integrated server player exists");
                    survey = MineralSurveyData.create(MineralSurveyIndex.version(server), ResourceLocation.withDefaultNamespace("plains"),
                            level.dimension().location(), List.of(new MineralEntry(ResourceLocation.withDefaultNamespace("iron_ore"), 1, -64, 64, 9, MineralEntry.Confidence.EXACT)));
                    level.setBlockAndUpdate(normalPos, JDTEBlocks.MINERAL_EXTRACTOR.get().defaultBlockState());
                    level.setBlockAndUpdate(largePos, JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
                    LargeMineralExtractorStructure.placeParts(level, largePos, Direction.NORTH);
                    for (BlockPos pos : List.of(normalPos, largePos)) {
                        var be = (MineralExtractorBE) level.getBlockEntity(pos);
                        var upgrades = UpgradeHelper.getUpgradeHandler(be);
                        upgrades.insertItem(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false);
                        upgrades.insertItem(1, new ItemStack(JDTEItems.FILTER_UPGRADE.get()), false);
                        be.getEnergyStorage().setEnergy(500_000);
                        be.getRedstoneControlData().redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;
                        ItemStack template = new ItemStack(JDTEItems.MINERAL_SURVEY.get());
                        MineralSurveyItem.setSurvey(template, survey); be.getMachineHandler().setStackInSlot(0, template);
                        be.getMachineHandler().setStackInSlot(be.outputStartSlot() + 48, new ItemStack(Items.IRON_ORE, 6_400_000));
                        be.getTimeFluidTank().setFluid(new net.minecraftforge.fluids.FluidStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(), 80_000));
                    }
                    player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
                    player.getAbilities().flying = true; player.onUpdateAbilities();
                });
                stage = -1; changedAt = ticks;
            }
            case -1 -> {
                if (!(mc.level.getBlockEntity(normalPos) instanceof MineralExtractorBE) || elapsed < 20) return;
                var server = mc.getSingleplayerServer(); var id = mc.player.getUUID();
                server.execute(() -> JDTEBlocks.MINERAL_EXTRACTOR.get().openMenu(server.getPlayerList().getPlayer(id), normalPos));
                stage = 1; changedAt = ticks;
            }
            case 1 -> {
                if (!(mc.screen instanceof MineralExtractorScreen) || !(mc.player.containerMenu instanceof MineralExtractorContainer menu) || elapsed < 40) return;
                check(((MineralExtractorScreen) mc.screen).getGuiTop() >= 44, "title remains visible at default GUI scale");
                check(menu.MACHINE_SLOTS == 17 && menu.slots.size() == 70, "normal visible menu layout");
                check(menu.getTimeFluid() == 80_000, "32-bit fluid amount crosses real container packets");
                MineralPackets.requestPage(menu.containerId, 3); next();
            }
            case 2 -> {
                if (elapsed < 15) return;
                var menu = (MineralExtractorContainer) mc.player.containerMenu;
                check(menu.getOutputPage() == 3, "server page acknowledgement");
                check(menu.slots.get(1).getItem().getCount() == 6_400_000, "count-safe output snapshot");
                mc.gameMode.handleInventoryMouseClick(menu.containerId, 1, 0, ClickType.PICKUP, mc.player); next();
            }
            case 3 -> {
                if (elapsed < 20) return;
                var menu = (MineralExtractorContainer) mc.player.containerMenu;
                check(menu.getCarried().getCount() == 64, "real cursor packet carries 64 items");
                check(menu.slots.get(1).getItem().getCount() == 6_399_936, "remaining oversized output stays synchronized");
                screenshot(mc, "jdte-mineral-normal.png");
                // Return the cursor before opening another menu to preserve the player's items.
                mc.gameMode.handleInventoryMouseClick(menu.containerId, menu.slots.size() - 1, 0, ClickType.PICKUP, mc.player);
                mc.options.guiScale().set(1); mc.resizeDisplay();
                next();
            }
            case 4 -> {
                if (elapsed < 15) return;
                check(mc.screen instanceof MineralExtractorScreen resized && resized.getGuiTop() == resized.expectedGuiTop(), "mineral layout survives GUI scale change");
                mc.options.guiScale().set(2); mc.resizeDisplay();
                var server = mc.getSingleplayerServer(); var id = mc.player.getUUID();
                server.execute(() -> JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get().openMenu(server.getPlayerList().getPlayer(id), largePos)); next();
            }
            case 5 -> {
                if (!(mc.screen instanceof MineralExtractorScreen) || !(mc.player.containerMenu instanceof MineralExtractorContainer menu) || menu.getSurveySlotCount() != 4 || elapsed < 30) return;
                check(menu.MACHINE_SLOTS == 20 && menu.slots.size() == 73, "large visible menu layout");
                MineralPackets.requestPage(menu.containerId, 3); next();
            }
            case 6 -> {
                if (elapsed < 15) return;
                var menu = (MineralExtractorContainer) mc.player.containerMenu;
                check(menu.getOutputPage() == 3 && menu.slots.get(4).getItem().getCount() == 6_400_000, "large page network snapshot");
                screenshot(mc, "jdte-mineral-large.png");
                mc.player.closeContainer(); next();
            }
            case 7 -> {
                if (elapsed < 15) return;
                var server = mc.getSingleplayerServer(); var id = mc.player.getUUID();
                server.execute(() -> MineralPackets.sendSurvey(server.getPlayerList().getPlayer(id), survey)); next();
            }
            case 8 -> {
                if (!(mc.screen instanceof MineralSurveyScreen) || elapsed < 30) return;
                check(MineralSurveyClientCache.isSynced() && !MineralSurveyClientCache.get().isEmpty(), "server biome catalog reaches client");
                screenshot(mc, "jdte-mineral-survey.png"); next();
            }
            case 9 -> {
                if (elapsed < 20) return;
                try {
                    var field = com.jdte.common.jei.JDTEJeiPlugin.class.getDeclaredField("runtime"); field.setAccessible(true);
                    var runtime = (mezz.jei.api.runtime.IJeiRuntime) field.get(null);
                    check(runtime != null, "JEI runtime exists");
                    var type = com.jdte.client.jei.mineralextractor.MineralExtractorRecipeCategory.RECIPE_TYPE;
                    check(runtime.getRecipeManager().createRecipeLookup(type).get().findAny().isPresent(), "JEI has synchronized mineral recipes");
                    runtime.getRecipesGui().showTypes(List.of(type));
                } catch (ReflectiveOperationException exception) { throw new IllegalStateException("Cannot inspect JEI smoke runtime", exception); }
                next();
            }
            case 10 -> {
                if (elapsed < 30) return;
                screenshot(mc, "jdte-mineral-jei.png"); mc.setScreen(null);
                aimAt(mc, normalPos); next();
            }
            case 11 -> {
                aimAt(mc, normalPos);
                if (elapsed < 50) return;
                var data = snownee.jade.impl.ObjectDataCenter.getServerData();
                if (data == null || !data.contains("jdte_extractor_state")) {
                    if (elapsed < 150) return;
                    throw new IllegalStateException("Mineral client smoke: Jade mineral server data missing; hit=" + mc.hitResult + ", data=" + data);
                }
                screenshot(mc, "jdte-mineral-jade.png");
                var partPos = largePos.relative(Direction.EAST);
                aimAt(mc, partPos);
                stage = 111; changedAt = ticks;
            }
            case 111 -> {
                aimAt(mc, largePos.relative(Direction.EAST));
                if (elapsed < 50) return;
                var accessor = snownee.jade.impl.ObjectDataCenter.get();
                var data = snownee.jade.impl.ObjectDataCenter.getServerData();
                boolean matchesPart = accessor instanceof snownee.jade.api.BlockAccessor block
                        && block.getBlock() == JDTEBlocks.LARGE_MINERAL_EXTRACTOR_PART.get();
                if (!matchesPart || data == null || !data.contains("jdte_extractor_state") || data.getList("jdte_upgrades", 10).isEmpty()) {
                    if (elapsed < 150) return;
                    throw new IllegalStateException("Mineral client smoke: large-part Jade data missing; hit=" + mc.hitResult + ", data=" + data);
                }
                screenshot(mc, "jdte-mineral-jade-part.png");
                var server = mc.getSingleplayerServer(); var id = mc.player.getUUID();
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayer(id);
                    var bag = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
                    var contents = new com.jdte.common.containers.handlers.UpgradeStorageHandler(bag);
                    contents.insertItem(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 24), false);
                    contents.insertItem(1, new ItemStack(com.direwolf20.justdirethings.setup.Registration.UPGRADE_SMELTER.get(), 64), false);
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bag);
                    player.inventoryMenu.broadcastChanges();
                    JDTEItems.UPGRADE_STORAGE.get().use(player.level(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                }); stage = 12; changedAt = ticks;
            }
            case 12 -> {
                if (!(mc.player.containerMenu instanceof com.jdte.common.containers.UpgradeStorageContainer menu) || elapsed < 25) return;
                check(mc.screen instanceof com.jdte.client.screens.UpgradeStorageScreen, "upgrade storage screen registered");
                check(menu.slots.size() == 56 && menu.stillValid(mc.player), "storage binding survives real initial inventory packets");
                check(menu.slots.get(1).getItem().getCount() == 64, "compressed native upgrades synchronize");
                mc.gameMode.handleInventoryMouseClick(menu.containerId, 0, 0, ClickType.PICKUP, mc.player); next();
            }
            case 13 -> {
                if (elapsed < 20) return;
                var menu = (com.jdte.common.containers.UpgradeStorageContainer) mc.player.containerMenu;
                check(menu.getCarried().getCount() == 24 && menu.slots.get(0).getItem().isEmpty(), "storage extraction reaches server and client");
                int free = java.util.stream.IntStream.range(20, 56).filter(i -> menu.slots.get(i).getItem().isEmpty()).findFirst().orElseThrow();
                mc.gameMode.handleInventoryMouseClick(menu.containerId, free, 0, ClickType.PICKUP, mc.player); next();
            }
            case 14 -> {
                if (elapsed < 20) return;
                var menu = (com.jdte.common.containers.UpgradeStorageContainer) mc.player.containerMenu;
                check(menu.getCarried().isEmpty() && menu.stillValid(mc.player), "storage remains usable after updated bag packets");
                var bag = mc.player.getMainHandItem();
                check(new com.jdte.common.containers.handlers.UpgradeStorageHandler(bag).getStackInSlot(0).isEmpty(), "bag NBT reflects server extraction");
                screenshot(mc, "jdte-upgrade-storage.png"); next();
            }
            case 15 -> {
                if (elapsed < 15) return;
                mc.player.closeContainer();
                var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
                server.execute(()->{
                    var player=server.getPlayerList().getPlayer(id);
                    var bag=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());
                    var contents=new com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler(bag);
                    contents.insertItem(35,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(),64),false);
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,bag);player.inventoryMenu.broadcastChanges();
                    JDTEItems.ADVANCED_UPGRADE_STORAGE.get().use(player.level(),player,net.minecraft.world.InteractionHand.MAIN_HAND);
                });next();
            }
            case 16 -> {
                if (!(mc.player.containerMenu instanceof com.jdte.common.containers.AdvancedUpgradeStorageContainer menu)||elapsed<25) return;
                check(mc.screen instanceof com.jdte.client.screens.AdvancedUpgradeStorageScreen,"advanced bag screen");
                check(menu.slots.size()==72&&menu.handler.getStackInSlot(35).getCount()==64,"36-slot advanced contents synchronized");
                screenshot(mc,"jdte-advanced-upgrade-storage.png");mc.player.closeContainer();stage=161;changedAt=ticks;
            }
            case 161 -> {
                if(elapsed<15)return;
                var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
                server.execute(()->{
                    var player=server.getPlayerList().getPlayer(id);
                    var machine=(MineralExtractorBE)player.serverLevel().getBlockEntity(normalPos);
                    com.jdte.common.upgrades.UpgradeHelper.getUpgradeHandler(machine).setStackInSlot(0,ItemStack.EMPTY);
                    ((com.jdte.common.blocks.MineralExtractorBlock)machine.getBlockState().getBlock()).openMenu(player,normalPos);
                });stage=17;changedAt=ticks;
            }
            case 17 -> {
                if (!(mc.screen instanceof MineralExtractorScreen screen)||elapsed<30) return;
                var area=com.jdte.client.screens.util.AdvancedUpgradeStorageScreenHelper.getPanelArea(screen);
                check(area!=null,"machine bag panel visible");
                screenshot(mc,"jdte-advanced-storage-machine-panel.png");
                double x=area.getX()+6+5*18+9,y=area.getY()+16+5*18+9;
                check(screen.mouseClicked(x,y,0),"machine bag panel click handled");next();
            }
            case 18 -> {
                if(elapsed<25)return;
                var menu=(MineralExtractorContainer)mc.player.containerMenu;
                check(menu.baseMachineBE!=null&&UpgradeHelper.countUpgrades(menu.baseMachineBE,com.jdte.common.upgrades.UpgradeType.CAPACITY)==1,"panel packet installed card");
                check(new com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler(mc.player.getMainHandItem()).getStackInSlot(35).getCount()==63,"panel packet conserved bag count");
                mc.player.closeContainer();
                var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
                server.execute(()->{
                    var player=server.getPlayerList().getPlayer(id);
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get()));player.inventoryMenu.broadcastChanges();
                    var bulk=new com.jdte.common.entities.BulkItemEntity(player.level(),player.getX()+1,player.getY(),player.getZ(),new ItemStack(Items.IRON_ORE,6_400_000));
                    bulk.setPickUpDelay(32767);player.serverLevel().addFreshEntity(bulk);
                });next();
            }
            case 19 -> {
                if(elapsed<25)return;
                boolean bulkSynced=java.util.stream.StreamSupport.stream(mc.level.entitiesForRendering().spliterator(),false)
                        .anyMatch(e->e instanceof com.jdte.common.entities.BulkItemEntity bulk&&bulk.getItem().getCount()==6_400_000);
                check(bulkSynced,"bulk drop uses integer entity synchronization");
                mc.gameMode.useItem(mc.player,net.minecraft.world.InteractionHand.MAIN_HAND);next();
            }
            case 20 -> {
                if(elapsed<25)return;
                check(mc.screen instanceof com.direwolf20.justdirethings.client.screens.MachineSettingsCopierScreen,"advanced copier uses native options screen");
                screenshot(mc,"jdte-advanced-settings-copier.png");
                copierAreaBefore=com.direwolf20.justdirethings.common.items.MachineSettingsCopier.getCopyArea(mc.player.getMainHandItem());
                var button=(net.minecraft.client.gui.components.AbstractButton)mc.screen.children().stream().filter(e->e instanceof net.minecraft.client.gui.components.AbstractButton).findFirst().orElseThrow();
                button.onPress();next();
            }
            case 21 -> {
                if(elapsed<25)return;
                check(com.direwolf20.justdirethings.common.items.MachineSettingsCopier.getCopyArea(mc.player.getMainHandItem())!=copierAreaBefore,"copier option reaches server and synchronizes inventory");
                mc.player.closeContainer();
                mc.getSingleplayerServer().execute(()->{
                    var player=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(JDTEItems.CRAFTING_PATTERN.get()));
                    player.getInventory().setItem(9,new ItemStack(Items.OAK_PLANKS,32));player.inventoryMenu.broadcastChanges();
                });next();
            }
            case 22 -> {
                if(elapsed<30)return;
                check(mc.player.getMainHandItem().is(JDTEItems.CRAFTING_PATTERN.get()),"pattern inventory sync");
                mc.gameMode.useItem(mc.player,net.minecraft.world.InteractionHand.MAIN_HAND);next();
            }
            case 23 -> {
                if(elapsed<25)return;
                check(mc.screen instanceof com.jdte.client.screens.CraftingPatternScreen,"real crafting-pattern screen");
                var menu=mc.player.containerMenu;
                mc.gameMode.handleInventoryMouseClick(menu.containerId,10,0,ClickType.PICKUP,mc.player);
                mc.gameMode.handleInventoryMouseClick(menu.containerId,0,0,ClickType.PICKUP,mc.player);
                mc.gameMode.handleInventoryButtonClick(menu.containerId,0);next();
            }
            case 24 -> {
                if(elapsed<35)return;
                var menu=mc.player.containerMenu;
                check(menu.getCarried().is(Items.OAK_PLANKS)&&menu.getCarried().getCount()==32,"ghost click preserves all ingredients");
                check(menu.slots.get(9).getItem().is(Items.OAK_BUTTON),"synced server recipe result");
                check(com.jdte.common.items.CraftingPatternItem.getPattern(mc.player.getMainHandItem())!=null,"server-saved pattern synchronizes to client");
                screenshot(mc,"jdte-crafting-pattern.png");
                mc.gameMode.handleInventoryMouseClick(menu.containerId,10,0,ClickType.PICKUP,mc.player);
                mc.player.closeContainer();next();
            }
            case 25 -> {
                if(elapsed<20)return;
                mixerPos=normalPos.above(4);
                mc.getSingleplayerServer().execute(()->{
                    var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayers().get(0);var level=player.serverLevel();
                    for(int x=-3;x<=4;x++)for(int z=-2;z<=4;z++)level.setBlockAndUpdate(mixerPos.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                    for(int i=0;i<2;i++){
                        var pos=mixerPos.east(i*2);level.setBlockAndUpdate(pos,(i==0?JDTEBlocks.ADVANCED_FLUID_MIXER.get():JDTEBlocks.EXTENDED_FLUID_MIXER.get()).defaultBlockState());
                        var mixer=(com.jdte.common.blockentities.FluidMixerBE)level.getBlockEntity(pos);var upgrades=UpgradeHelper.getUpgradeHandler(mixer);
                        upgrades.insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);upgrades.insertItem(1,new ItemStack(JDTEItems.AE_CRAFTING_READ_UPGRADE.get()),false);
                        mixer.inputTankA.setFluid(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,120000));
                        mixer.inputTankB.setFluid(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.LAVA,80000));
                        mixer.outputTank.setFluid(new net.minecraftforge.fluids.FluidStack(net.minecraft.core.registries.BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("justdirethings","time_fluid")),100000));
                        mixer.getEnergyStorage().receiveEnergy(50000,false);mixer.markDirtyClient();
                    }
                    player.teleportTo(mixerPos.getX()+0.5,mixerPos.getY(),mixerPos.getZ()+3.5);player.getInventory().setItem(9,new ItemStack(JDTEItems.MIXING_UPGRADE.get()));player.getInventory().setItem(10,new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get()));player.inventoryMenu.broadcastChanges();
                });next();
            }
            case 26 -> {
                if(elapsed<40 || mc.level.getBlockEntity(mixerPos)==null || mc.level.getBlockEntity(mixerPos.east(2))==null)return;
                mc.getSingleplayerServer().execute(()->JDTEBlocks.ADVANCED_FLUID_MIXER.get().openMenu(mc.getSingleplayerServer().getPlayerList().getPlayers().get(0),mixerPos));next();
            }
            case 27 -> {
                if(elapsed<30)return;
                check(mc.screen instanceof com.jdte.client.screens.AdvancedFluidMixerScreen,"actual advanced mixer screen");
                var menu=(com.jdte.common.containers.FluidMixerContainer)mc.player.containerMenu;
                check(menu.getInputAFluidStack().getAmount()==120000&&menu.getInputBFluidAmount()==80000&&menu.getOutputFluidAmount()==100000&&menu.getFluidCapacity()==400000,"three actual network tank values retain high bits");
                if(!mixerCursorPicked) {
                    check(menu.getCarried().isEmpty(),"clean cursor before mixer interaction");
                    int slot=java.util.stream.IntStream.range(0,menu.slots.size()).filter(i->menu.slots.get(i).getItem().is(JDTEItems.MIXING_UPGRADE.get())).findFirst().orElseThrow();
                    mc.gameMode.handleInventoryMouseClick(menu.containerId,slot,0,ClickType.QUICK_MOVE,mc.player);
                    int catalyst=java.util.stream.IntStream.range(1,menu.slots.size()).filter(i->menu.slots.get(i).getItem().is(JDTEItems.TIME_FLUID_CATALYST.get())).findFirst().orElseThrow();
                    mc.gameMode.handleInventoryMouseClick(menu.containerId,catalyst,0,ClickType.PICKUP,mc.player);
                    mixerCursorPicked=true;return;
                }
                if(elapsed<40)return;
                check(menu.getCarried().is(JDTEItems.TIME_FLUID_CATALYST.get()),"catalyst cursor survived server synchronization before screen click: "+menu.getCarried());
                var screen=(com.jdte.client.screens.AdvancedFluidMixerScreen)mc.screen;
                check(screen.getGuiTop()==screen.expectedGuiTop()&&screen.getGuiTop()>=44,"mixer title remains visible after actual GUI resize");
                // Click the displayed slot, rather than bypassing its screen hit box.
                screen.mouseClicked(screen.getGuiLeft()-30+94+8,screen.getGuiTop()-26+14+8,0);
                screen.mouseReleased(screen.getGuiLeft()-30+94+8,screen.getGuiTop()-26+14+8,0);
                LOG.info("Mixer click: origin {},{} slot {},{} carried {} catalyst {}",screen.getGuiLeft(),screen.getGuiTop(),menu.slots.get(0).x,menu.slots.get(0).y,menu.getCarried(),menu.slots.get(0).getItem());
                screenshot(mc,"jdte-mixer-click-diagnostic.png");next();
            }
            case 28 -> {
                if(elapsed<30)return;
                check(UpgradeHelper.hasMixingUpgrade((com.jdte.common.blockentities.FluidMixerBE)mc.level.getBlockEntity(mixerPos)),"vanilla Shift packet installs plain mixing card and synchronizes upgrade");
                check(mc.player.containerMenu.slots.get(0).getItem().is(JDTEItems.TIME_FLUID_CATALYST.get())&&mc.player.containerMenu.getCarried().isEmpty(),"displayed catalyst slot accepts actual mouse click and syncs; actual="+mc.player.containerMenu.slots.get(0).getItem()+", carried="+mc.player.containerMenu.getCarried());
                screenshot(mc,"jdte-advanced-fluid-mixer.png");
                mc.getSingleplayerServer().execute(()->UpgradeHelper.getUpgradeHandler((com.jdte.common.blockentities.FluidMixerBE)mc.getSingleplayerServer().overworld().getBlockEntity(mixerPos)).extractItem(0,1,false));next();
            }
            case 29 -> {
                if(elapsed<30)return;
                var menu=(com.jdte.common.containers.FluidMixerContainer)mc.player.containerMenu;
                check(menu.getFluidCapacity()==8000&&menu.getInputAFluidStack().getAmount()==8000&&menu.getInputBFluidAmount()==8000&&menu.getOutputFluidAmount()==8000,"capacity removal updates and clamps all three displayed tanks");
                screenshot(mc,"jdte-fluid-mixer-capacity-clamp.png");mc.player.closeContainer();
                mc.getSingleplayerServer().execute(()->JDTEBlocks.EXTENDED_FLUID_MIXER.get().openMenu(mc.getSingleplayerServer().getPlayerList().getPlayers().get(0),mixerPos.east(2)));next();
            }
            case 30 -> {
                if(elapsed<30)return;
                check(mc.screen instanceof com.jdte.client.screens.ExtendedFluidMixerScreen,"actual extended mixer screen");
                var menu=(com.jdte.common.containers.FluidMixerContainer)mc.player.containerMenu;check(menu.getFluidCapacity()==400000&&menu.getInputBFluidAmount()==80000,"extended mixer data synchronized");
                check(UpgradeHelper.getUpgradeHandler((com.jdte.common.blockentities.FluidMixerBE)mc.level.getBlockEntity(mixerPos.east(2))).getSlots()==8,"extended mixer exposes eight upgrade slots");
                screenshot(mc,"jdte-extended-fluid-mixer.png");
                try{
                    var field=com.jdte.common.jei.JDTEJeiPlugin.class.getDeclaredField("runtime");field.setAccessible(true);var runtime=(mezz.jei.api.runtime.IJeiRuntime)field.get(null);
                    var type=com.jdte.client.jei.fluidmixer.FluidMixerRecipeCategory.RECIPE_TYPE;
                    check(runtime.getRecipeManager().createRecipeLookup(type).get().anyMatch(recipe->recipe.id().equals(ResourceLocation.fromNamespaceAndPath("jdte","fluid_mixer/time_fluid_mixing"))&&!recipe.output().isEmpty()),"JEI displays real synced mixer recipe with stable ID/product");
                    runtime.getRecipesGui().showTypes(List.of(type));
                }catch(ReflectiveOperationException error){throw new IllegalStateException(error);}next();
            }
            case 31 -> {
                if(elapsed<25)return;screenshot(mc,"jdte-fluid-mixer-jei.png");
                try { Files.writeString(mc.gameDirectory.toPath().resolve("mineral-smoke-success.txt"), "PASS: normal/large/survey, fluids/pages/count packets, JEI/Jade, basic/advanced bags, machine panel packet insertion/conservation advanced copier option packets 6.4M bulk-drop entity sync real crafting-pattern ghost/save packets and both mixer menus/high-bit tank data/capacity clamp/Shift mixing insertion/JEI\n"); }
                catch(java.io.IOException exception){throw new IllegalStateException(exception);}
                LOG.info("JDTE_MINERAL_CLIENT_SMOKE_PASS");mc.stop();next();
            }
            default -> { }
        }
    }
}
