package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.util.ExperienceUtils;
import com.jdte.common.containers.ExtendedExperienceHolderContainer;
import com.jdte.common.network.JDTEPacketHandler;
import com.jdte.common.network.data.ExtendedExperienceHolderSettingsPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Native JDT controls, with full-width server-menu XP rather than a possibly stale client BE. */
public class ExtendedExperienceHolderScreen extends BaseMachineScreen<ExtendedExperienceHolderContainer> {
    private NumberButton target;
    private int pendingTargetTicks,lastSyncedTarget;
    private com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton owner,collect,particles;
    public ExtendedExperienceHolderScreen(ExtendedExperienceHolderContainer menu,Inventory inventory,Component title){super(menu,inventory,title);}
    @Override public void init(){
        super.init();
        lastSyncedTarget=menu.targetXp();
        int x=topSectionLeft+topSectionWidth/2, y=topSectionTop;
        var layout=com.jdte.common.utils.GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.STOREEXPBUTTON(x+layout.getExperienceWidget("store_x",15),y+layout.getExperienceWidget("buttons_y",62),true,b->send(Screen.hasControlDown()?2:Screen.hasShiftDown()?1:0)));
        addRenderableWidget(ToggleButtonFactory.EXTRACTEXPBUTTON(x+layout.getExperienceWidget("extract_x",-33),y+layout.getExperienceWidget("buttons_y",62),true,b->send(Screen.hasControlDown()?5:Screen.hasShiftDown()?4:3)));
        target=addRenderableWidget(ToggleButtonFactory.TARGETEXPBUTTON(x+layout.getExperienceWidget("target_x",-57),y+layout.getExperienceWidget("target_y",64),menu.targetXp(),b->{pendingTargetTicks=40;JDTEPacketHandler.CHANNEL.sendToServer(new ExtendedExperienceHolderSettingsPayload(menu.getBlockPos(),((NumberButton)b).getValue()));}));
        owner=addRenderableWidget(ToggleButtonFactory.OWNERONLYBUTTON(x+layout.getExperienceWidget("owner_x",-75),y+layout.getExperienceWidget("buttons_y",62),menu.ownerOnly(),b->send(6)));
        collect=addRenderableWidget(ToggleButtonFactory.COLLECTEXPBUTTON(x+layout.getExperienceWidget("collect_x",15),y+layout.getExperienceWidget("toggles_y",42),menu.collectExp(),b->send(7)));
        particles=addRenderableWidget(ToggleButtonFactory.SHOWPARTICLESBUTTON(x+layout.getExperienceWidget("particles_x",31),y+layout.getExperienceWidget("toggles_y",42),menu.showParticles(),b->send(8)));
    }
    private void send(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override protected void containerTick(){super.containerTick();if(menu.targetXp()!=lastSyncedTarget||pendingTargetTicks<=0){target.setValue(menu.targetXp());lastSyncedTarget=menu.targetXp();pendingTargetTicks=0;}else pendingTargetTicks--;if(owner.getButtonActive()!=menu.ownerOnly())owner.toggleActive();if(collect.getButtonActive()!=menu.collectExp())collect.toggleActive();if(particles.getButtonActive()!=menu.showParticles())particles.toggleActive();}
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){
        super.renderBg(graphics,partial,mouseX,mouseY);
        lastSyncedTarget=menu.targetXp();
        int x=topSectionLeft+(topSectionWidth-182)/2,y=topSectionTop+topSectionHeight-15;
        var texture=new ResourceLocation("minecraft","textures/gui/icons.png");
        graphics.blit(texture,x,y,0,64,182,5);
        int progress=(int)(ExperienceUtils.getProgressToNextLevel(menu.storedXp())*183);
        if(progress>0)graphics.blit(texture,x,y,0,69,Math.min(182,progress),5);
        graphics.drawCenteredString(font,Integer.toString(ExperienceUtils.getLevelFromTotalExperience(menu.storedXp())),topSectionLeft+topSectionWidth/2,topSectionTop+66,8453920);

    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){
        super.render(graphics,mouseX,mouseY,partial);
        int x=topSectionLeft+(topSectionWidth-182)/2,y=topSectionTop+topSectionHeight-15;
        if(mouseX>=x&&mouseX<x+182&&mouseY>=y&&mouseY<y+8)
            graphics.renderTooltip(font,Component.translatable("jdte.screen.experience_holder.stored",menu.storedXp()),mouseX,mouseY);
    }

}
