package com.nekiak.masterghoster9000;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;


@Mod(
        modid = MasterGhoster.MOD_ID,
        name = MasterGhoster.MOD_NAME,
        version = MasterGhoster.VERSION
)
public class MasterGhoster {
    public static final String MOD_ID = "loader";
    public static final String MOD_NAME = "loader";
    public static final String VERSION = "loader";

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

    }

    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent ev) {

    }
}