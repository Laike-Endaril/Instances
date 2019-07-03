package com.fantasticsource.instances;

import com.fantasticsource.instances.client.ClientHandler;
import com.fantasticsource.instances.instancetypes.InstanceTypes;
import com.fantasticsource.instances.instancetypes.voided.BiomeVoid;
import com.fantasticsource.instances.network.PacketHandler;
import com.fantasticsource.instances.server.InstanceHandler;
import com.fantasticsource.instances.server.commands.CommandTeleportD;
import com.fantasticsource.instances.server.commands.CommandTimeD;
import com.fantasticsource.instances.server.commands.CommandWeatherD;
import com.fantasticsource.instances.server.commands.Commands;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

@Mod(modid = Instances.MODID, name = Instances.NAME, version = Instances.VERSION)
public class Instances
{
    public static final String MODID = "instances";
    public static final String NAME = "Instances";
    public static final String VERSION = "1.12.2.000";

    public static Integer nextFreeDimID()
    {
        for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++)
        {
            if (!DimensionManager.isDimensionRegistered(i))
            {
                return i;
            }
        }
        return null;
    }

    public static Integer nextFreeDimTypeID()
    {
        DimensionType[] dimensionTypes = DimensionType.values();
        int count = dimensionTypes.length;

        int[] ints = new int[count];
        for (int i = 0; i < count; i++)
        {
            ints[i] = dimensionTypes[i].getId();
        }

        for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++)
        {
            boolean found = false;
            for (int i2 : ints)
                if (i2 == i)
                {
                    found = true;
                    break;
                }
            if (!found) return i;
        }

        return null;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);

        //Biomes
        BiomeVoid.init();

        //Instance Types
        InstanceTypes.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        PacketHandler.init();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new Commands());
        event.registerServerCommand(new CommandWeatherD());
        event.registerServerCommand(new CommandTimeD());
        event.registerServerCommand(new CommandTeleportD());

        InstanceHandler.registerInstances();
    }

    @EventHandler
    public void serverStop(FMLServerStoppedEvent event)
    {
        InstanceHandler.unloadHandler();
    }

    @SubscribeEvent
    public void clientConnect(FMLNetworkEvent.ServerConnectionFromClientEvent event)
    {
        event.getManager().sendPacket(PacketHandler.INSTANCE.getPacketFrom(InstanceHandler.constructSyncMessage()));
    }

    @SubscribeEvent
    public void clientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        if (!event.getManager().isLocalChannel()) ClientHandler.cleanUp();
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            InstanceHandler.checkUnloadWorlds();
        }
    }
}
