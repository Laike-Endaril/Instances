package com.fantasticsource.instances;

import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import com.fantasticsource.instances.client.ClientHandler;
import com.fantasticsource.instances.client.LightFixer;
import com.fantasticsource.instances.commands.*;
import com.fantasticsource.instances.network.Network;
import com.fantasticsource.instances.network.messages.SyncInstancesPacket;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.boimes.BiomeVoid;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.MCTools;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

@Mod(modid = Instances.MODID, name = Instances.NAME, version = Instances.VERSION, dependencies = "required-after:fantasticlib@[1.12.2.021a,)")
public class Instances
{
    public static final String MODID = "instances";
    public static final String NAME = "Instances";
    public static final String VERSION = "1.12.2.000s";

    public static Integer nextFreeDimID()
    {
        for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++)
        {
            if (!DimensionManager.isDimensionRegistered(i)) return i;
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
            {
                if (i2 == i)
                {
                    found = true;
                    break;
                }
            }
            if (!found) return i;
        }

        return null;
    }

    public static void setPlayerMode(EntityPlayerMP player, InstanceWorldInfo info)
    {
        //Preserve gamemode for OP players
        if (MCTools.isOP(player)) return;

        if (info == null)
        {
            //Not an instance dimension; use the dimension gametype
            player.setGameType(player.world.getWorldInfo().getGameType());
            return;
        }

        if (player.getPersistentID().equals(info.getOwner())) player.setGameType(GameType.SURVIVAL);
        else player.setGameType(GameType.ADVENTURE);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException
    {
        Converter.convert();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(BlocksAndItems.class);

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            //Physical client
            MinecraftForge.EVENT_BUS.register(LightFixer.class);
        }


        //Biomes
        BiomeVoid.init();

        //Instance Types
        InstanceTypes.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        Network.init();
    }

    @EventHandler
    public void serverStartingPre(FMLServerAboutToStartEvent event)
    {
        if (!event.getServer().getAllowNether()) throw new IllegalStateException("The Instances mod cannot run with allow-nether set to false in server.properties! (MC bug: https://bugs.mojang.com/browse/MC-85267)");
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) throws IOException
    {
        event.registerServerCommand(new Commands());
        event.registerServerCommand(new CmdDimWeather());
        event.registerServerCommand(new CmdDimTime());
        event.registerServerCommand(new CmdDimTP());
        event.registerServerCommand(new CmdEscape());
        event.registerServerCommand(new CmdVisitors());

        InstanceHandler.init(event);
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) throws IOException
    {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers())
        {
            Teleport.escape(player);
        }

        InstanceHandler.clear();
    }

    @SubscribeEvent
    public void clientConnect(FMLNetworkEvent.ServerConnectionFromClientEvent event)
    {
        event.getManager().sendPacket(Network.WRAPPER.getPacketFrom(new SyncInstancesPacket()));
    }

    @SubscribeEvent
    public void clientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        if (!event.getManager().isLocalChannel()) ClientHandler.cleanUp();
    }

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        Teleport.escape(player);
        setPlayerMode(player, InstanceHandler.get(player.world.provider.getDimension()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void playerInteract(PlayerInteractEvent.RightClickBlock event)
    {
        World world = event.getWorld();

        DimensionType dimType = world.provider.getDimensionType();
        if (dimType == InstanceTypes.skyroomDimType)
        {
            GameType gameType = world.isRemote ? Minecraft.getMinecraft().playerController.getCurrentGameType() : ((EntityPlayerMP) event.getEntityPlayer()).interactionManager.getGameType();
            if (gameType == GameType.ADVENTURE)
            {
                //Visitor
                if (world.getBlockState(event.getPos()).getBlock().getRegistryName().toString().contains("armourers_workshop:tile.skinnable"))
                {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void entityDamagePre(LivingHurtEvent event)
    {
        Entity entity = event.getEntity();
        DimensionType dimType = event.getEntity().world.provider.getDimensionType();
        if (dimType == InstanceTypes.libraryOfWorldsDimType || dimType == InstanceTypes.skyroomDimType)
        {
            //Cancel damage
            event.setAmount(0);
            event.setCanceled(true);

            //Teleport out of the void if need be
            if (entity.posY < 0 && entity instanceof EntityPlayer)
            {
                if (dimType == InstanceTypes.libraryOfWorldsDimType) Teleport.gotoHub((EntityPlayerMP) entity);
                else Teleport.joinPossiblyCreating(entity, entity.dimension);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void entityDamagePost(LivingHurtEvent event)
    {
        Entity entity = event.getEntity();
        DimensionType dimType = event.getEntity().world.provider.getDimensionType();
        if (dimType == InstanceTypes.libraryOfWorldsDimType || dimType == InstanceTypes.skyroomDimType)
        {
            //Cancel damage
            event.setAmount(0);
            event.setCanceled(true);

            //Teleport out of the void if need be
            if (entity.posY < 0 && entity instanceof EntityPlayer)
            {
                if (dimType == InstanceTypes.libraryOfWorldsDimType) Teleport.gotoHub((EntityPlayerMP) entity);
                else Teleport.joinPossiblyCreating(entity, entity.dimension);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerLoggedOff(PlayerEvent.PlayerLoggedOutEvent event)
    {
        Teleport.escape(event.player);
    }

    @SubscribeEvent
    public void worldUnload(WorldEvent.Unload event) throws IOException
    {
        World world = event.getWorld();
        if (world.isRemote) return;

        WorldInfo info = world.getWorldInfo();
        if (!(info instanceof InstanceWorldInfo)) return;

        InstanceHandler.unload((InstanceWorldInfo) info);
    }
}
