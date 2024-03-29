package com.fantasticsource.instances;

import com.fantasticsource.fantasticlib.api.FLibAPI;
import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import com.fantasticsource.instances.client.LightFixer;
import com.fantasticsource.instances.client.LocalDimensions;
import com.fantasticsource.instances.commands.*;
import com.fantasticsource.instances.network.Network;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.tags.entity.CurrentWorldname;
import com.fantasticsource.instances.tags.entity.EscapePoint;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.InstanceWorldProvider;
import com.fantasticsource.instances.world.WorldInstance;
import com.fantasticsource.instances.world.boimes.BiomeVoid;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.WorldEventDistributor;
import com.fantasticsource.tools.Tools;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;

@Mod(modid = Instances.MODID, name = Instances.NAME, version = Instances.VERSION, dependencies = "required-after:fantasticlib@[1.12.2.047,)")
public class Instances
{
    public static final String MODID = "instances";
    public static final String NAME = "Instances";
    public static final String VERSION = "1.12.2.001i";

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

    public static void setPlayerMode(EntityPlayerMP player, InstanceData data)
    {
        //Preserve gamemode for OP players
        if (MCTools.isOP(player)) return;

        if (data == null)
        {
            //Not an instance dimension; use the dimension gametype
            player.setGameType(player.world.getWorldInfo().getGameType());
            return;
        }

        if ((player.getPersistentID()).equals(data.getOwner())) player.setGameType(GameType.SURVIVAL);
        else player.setGameType(GameType.ADVENTURE);
    }

    @SubscribeEvent
    public static void saveConfig(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MODID)) ConfigManager.sync(MODID, Config.Type.INSTANCE);
    }

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {
        FLibAPI.attachNBTCapToWorldIf(MODID, world -> world instanceof WorldInstance);
        FLibAPI.attachNBTCapToEntityIf(MODID, entity -> true);

        MinecraftForge.EVENT_BUS.register(Instances.class);
        MinecraftForge.EVENT_BUS.register(BlocksAndItems.class);
        MinecraftForge.EVENT_BUS.register(CurrentWorldname.class);
        MinecraftForge.EVENT_BUS.register(InstanceWorldProvider.class);
        MinecraftForge.EVENT_BUS.register(WorldEventDistributor.class);
        MinecraftForge.EVENT_BUS.register(Commands.class);


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
    public static void init(FMLInitializationEvent event)
    {
        Network.init();
    }

    @EventHandler
    public static void serverStartingPre(FMLServerAboutToStartEvent event)
    {
        MinecraftServer server = event.getServer();

        if (!server.getAllowNether()) throw new IllegalStateException("The Instances mod cannot run with allow-nether set to false in server.properties! (MC bug: https://bugs.mojang.com/browse/MC-85267)");
    }

    @EventHandler
    public static void serverStarting(FMLServerStartingEvent event)
    {
        File file = new File(InstanceHandler.getInstancesDir(event.getServer()) + "Temporary");
        Tools.deleteFilesRecursively(file);

        event.registerServerCommand(new Commands());
        event.registerServerCommand(new CmdDimWeather());
        event.registerServerCommand(new CmdDimTime());
        event.registerServerCommand(new CmdEscape());
        event.registerServerCommand(new CmdVisitors());
    }

    @EventHandler
    public static void serverStopping(FMLServerStoppingEvent event)
    {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers())
        {
            Teleport.escape(player);
        }
    }

    @SubscribeEvent
    public static void clientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        if (!event.getManager().isLocalChannel()) LocalDimensions.resetDimensionManager();
    }

    @SubscribeEvent
    public static void serverConnectionFromClient(FMLNetworkEvent.ServerConnectionFromClientEvent event)
    {
        NetHandlerPlayServer netHandler = (NetHandlerPlayServer) event.getManager().getNetHandler();
        EntityPlayerMP player = netHandler.player;
        int dim = player.dimension;
        event.getManager().sendPacket(Network.WRAPPER.getPacketFrom(new Network.SyncDimensionTypePacket(dim)));


        //Check world name for match, and if it doesn't, set player dimension to an invalid one
        String worldName = CurrentWorldname.getCurrentWorldName(player);
        if (worldName != null && !worldName.equals(player.world.getWorldInfo().getWorldName())) dim = nextFreeDimTypeID();


        if (FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim) == null)
        {
            System.err.println(TextFormatting.RED + "This error was due to a player loading into a no-longer-existent dimension, which may happen if the server shut down forcefully, among other things");


            Destination escapePoint = EscapePoint.getEscapePoint(player);
            if (escapePoint != null)
            {
                Teleport.teleport(player, escapePoint);
                System.err.println(TextFormatting.RED + "The player (" + player.getName() + ") will end up at their last 'instance escape point': " + player.posX + ", " + player.posY + ", " + player.posZ + " in dimension " + player.dimension);
                return;
            }


            World overworld = FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0];
            BlockPos spawnPoint = overworld.provider.getRandomizedSpawnPoint();
            spawnPoint = overworld.getTopSolidOrLiquidBlock(spawnPoint);
            Teleport.teleport(player, overworld.provider.getDimension(), spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5, player.rotationYawHead, player.rotationPitch);
            System.err.println(TextFormatting.RED + "The player (" + player.getName() + ") will end up at spawn (no instance escape point was found)");
        }
    }

    @SubscribeEvent
    public static void playerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        Teleport.escape(player);
        setPlayerMode(player, InstanceData.get(MCTools.getSaveFolder(player.world.provider).replace("Instances" + File.separator, "")));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void playerInteract(PlayerInteractEvent.RightClickBlock event)
    {
        World world = event.getWorld();

        DimensionType dimType = world.provider.getDimensionType();
        if (dimType == InstanceTypes.SKYROOM)
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
    public static void entityDamagePre(LivingHurtEvent event)
    {
        checkCancelDamageAndPort(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityDamagePost(LivingHurtEvent event)
    {
        checkCancelDamageAndPort(event);
    }

    public static void checkCancelDamageAndPort(LivingHurtEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.world;
        DimensionType dimType = world.provider.getDimensionType();
        if (dimType == InstanceTypes.LIBRARY_OF_WORLDS || dimType == InstanceTypes.SKYROOM)
        {
            //Cancel damage
            event.setAmount(0);
            event.setCanceled(true);

            if (world.isRemote) return;


            //Teleport out of the void if need be
            if (entity.posY < 0) Teleport.teleport(entity, entity.dimension);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerLoggedOff(PlayerEvent.PlayerLoggedOutEvent event)
    {
        Teleport.escape(event.player);
    }

    @SubscribeEvent
    public static void worldUnload(WorldEvent.Unload event)
    {
        World world = event.getWorld();
        if (world.isRemote) return;

        WorldInfo info = world.getWorldInfo();
        if (!(info instanceof InstanceWorldInfo)) return;

        InstanceHandler.unload((InstanceWorldInfo) info);
    }
}
