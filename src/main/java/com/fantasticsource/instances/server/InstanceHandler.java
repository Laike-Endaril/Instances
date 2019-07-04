package com.fantasticsource.instances.server;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.network.messages.MessageDimensionSync;
import com.fantasticsource.instances.util.TeleporterSimple;
import com.fantasticsource.instances.util.WorldInfoSimple;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class InstanceHandler extends WorldSavedData
{
    public static LinkedHashMap<Integer, WorldInfoSimple> instanceInfo = new LinkedHashMap<>();
    private static String NAME = "InstanceHandler";
    private static InstanceHandler instanceHandler = null;

    public InstanceHandler(String name)
    {
        super(name);
    }

    public InstanceHandler()
    {
        this(NAME);
    }

    public static void registerInstances()
    {
        if (instanceHandler != null) return;

        instanceHandler = (InstanceHandler) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage().getOrLoadData(InstanceHandler.class, NAME);

        if (instanceHandler == null)
        {
            instanceHandler = new InstanceHandler();
            FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage().setData(NAME, instanceHandler);
        }

        for (Entry<Integer, WorldInfoSimple> entry : instanceInfo.entrySet())
        {
            int dimensionID = entry.getKey();
            WorldInfoSimple worldInfo = entry.getValue();

            DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());
            DimensionManager.keepDimensionLoaded(dimensionID, false);
        }
    }

    public static void unloadHandler()
    {
        instanceHandler = null;

        for (Map.Entry<Integer, WorldInfoSimple> entry : instanceInfo.entrySet())
        {
            int id = entry.getKey();
            DimensionManager.unregisterDimension(id);
        }
        instanceInfo.clear();
    }

    public static void createDimension(EntityPlayerMP playerEntity, WorldInfoSimple worldInfo)
    {
        int dimensionID = Instances.nextFreeDimID();
        instanceInfo.put(dimensionID, worldInfo);

        DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());

        playerEntity.sendMessage(new TextComponentString(String.format("Created %s using id %s", worldInfo.getWorldName(), dimensionID)).setStyle(new Style().setColor(TextFormatting.GREEN)));
    }

    public static void createDimension(ICommandSender sender, DimensionType type, EntityPlayer owner, String name)
    {
        int dimensionID = Instances.nextFreeDimID();

        WorldInfo tempInfo = DimensionManager.getWorld(0).getWorldInfo();
        WorldInfoSimple worldInfo = new WorldInfoSimple(new WorldSettings(new Random().nextLong(), tempInfo.getGameType(), true, false, tempInfo.getTerrainType()), name, type);

        instanceInfo.put(dimensionID, worldInfo);
        DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());

        sender.sendMessage(new TextComponentString(String.format("Created %s using id %s", worldInfo.getWorldName(), dimensionID)).setStyle(new Style().setColor(TextFormatting.GREEN)));
    }

    private static void loadDimension(int dimensionID, WorldInfo worldInfo)
    {
        WorldServer overworld = (WorldServer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
        if (overworld == null) throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!");

        try
        {
            DimensionManager.getProviderType(dimensionID);
        }
        catch (Exception e)
        {
            System.err.println("Cannot Hotload Dim: " + e.getMessage());
            return;
        }

        MinecraftServer mcServer = overworld.getMinecraftServer();
        ISaveHandler savehandler = overworld.getSaveHandler();
        EnumDifficulty difficulty = mcServer.getEntityWorld().getDifficulty();

        WorldServer world = (WorldServer) (new WorldCustom(worldInfo, mcServer, savehandler, dimensionID, overworld, mcServer.profiler).init());
        world.addEventListener(new ServerWorldEventHandler(mcServer, world));
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));

        if (!mcServer.isSinglePlayer())
        {
            world.getWorldInfo().setGameType(mcServer.getGameType());
        }

        mcServer.setDifficultyForAllWorlds(difficulty);
    }

    public static void unloadDimension(ICommandSender sender, int instanceID)
    {
        if (!instanceInfo.containsKey(instanceID))
        {
            if (sender != null) sender.sendMessage(new TextComponentString("Instance ID (" + instanceID + ") not found"));
            return;
        }

        WorldServer w = DimensionManager.getWorld(instanceID);

        if (w == null)
        {
            if (DimensionManager.isDimensionRegistered(instanceID))
            {
                loadDimension(instanceID, instanceInfo.get(instanceID));
                w = DimensionManager.getWorld(instanceID);
                if (w == null)
                {
                    if (sender != null) sender.sendMessage(new TextComponentString("Failed to load dimension").setStyle(new Style().setColor(TextFormatting.RED)));
                    return;
                }
            }
        }

        if (!w.playerEntities.isEmpty())
        {
            WorldServer overworld = DimensionManager.getWorld(0);
            MinecraftServer mcserver = overworld.getMinecraftServer();
            PlayerList plist = mcserver.getPlayerList();
            BlockPos defaultspawnpoint = overworld.getSpawnPoint();
            ArrayList<EntityPlayer> currentPlayers = new ArrayList<>(w.playerEntities);
            for (EntityPlayer player : currentPlayers)
            {
                BlockPos spawnpoint = new BlockPos(defaultspawnpoint.getX(), overworld.getHeight(defaultspawnpoint.getX(), defaultspawnpoint.getZ()), defaultspawnpoint.getZ());
                BlockPos bedlocation = player.getBedLocation();

                if (bedlocation != null)
                {
                    BlockPos bedspawnlocation = EntityPlayer.getBedSpawnLocation(overworld, bedlocation, false);
                    if (bedspawnlocation != null)
                    {
                        spawnpoint = bedspawnlocation;
                    }
                }

                EnumSet enumset = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);

                player.dismountRidingEntity();
                ((EntityPlayerMP) player).connection.setPlayerLocation(spawnpoint.getX(), spawnpoint.getY(), spawnpoint.getZ(), 0, 0, enumset);

                plist.transferPlayerToDimension((EntityPlayerMP) player, 0, new TeleporterSimple(overworld));

                if (sender != player)
                {
                    player.sendMessage(new TextComponentString("The dimension you were in was unloaded").setStyle(new Style().setColor(TextFormatting.RED)));
                }
            }
        }

        MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(w));
        w.flush();
        DimensionManager.setWorld(instanceID, null, w.getMinecraftServer());
        w.flush();
    }

    public static void deleteDimension(ICommandSender sender, int dimensionID)
    {
        unloadDimension(sender, dimensionID);


        DimensionManager.unregisterDimension(dimensionID);
        instanceInfo.remove(dimensionID);


        File dimensionFolder = new File(DimensionManager.getCurrentSaveRootDirectory(), "DIM" + dimensionID);

        try
        {
            FileUtils.deleteDirectory(dimensionFolder);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            if (sender != null) sender.sendMessage(new TextComponentString("Error deleting dimension folder of " + dimensionID + ". Has to be removed manually.").setStyle(new Style().setColor(TextFormatting.RED)));
        }
        finally
        {
            if (sender != null) sender.sendMessage(new TextComponentString("Completely deleted dimension " + dimensionID).setStyle(new Style().setColor(TextFormatting.GREEN)));
        }
    }

    public static IMessage constructSyncMessage()
    {
        MessageDimensionSync message = new MessageDimensionSync();

        for (Map.Entry<Integer, WorldInfoSimple> entry : instanceInfo.entrySet())
        {
            message.addDimension(entry.getKey(), entry.getValue().getDimensionType());
        }

        return message;
    }

    public static ArrayList<String> list()
    {
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<Integer, WorldInfoSimple> entry : instanceInfo.entrySet())
        {
            WorldInfoSimple info = entry.getValue();
            result.add(entry.getKey() + " (" + info.getWorldName() + ") Type = " + info.getDimensionType());
        }
        return result;
    }

    public static WorldInfoSimple get(int id)
    {
        return instanceInfo.get(id);
    }

    @Override
    public boolean isDirty()
    {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        NBTTagList nbtList = nbt.getTagList("instanceInfo", 10);

        for (int i = 0; i < nbtList.tagCount(); i++)
        {
            NBTTagCompound compound = nbtList.getCompoundTagAt(i);

            instanceInfo.put(compound.getInteger("dimensionID"), new WorldInfoSimple(compound.getCompoundTag("worldInfo")));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        NBTTagList nbtList = new NBTTagList();

        for (Entry<Integer, WorldInfoSimple> entry : instanceInfo.entrySet())
        {
            NBTTagCompound compound = new NBTTagCompound();

            compound.setInteger("dimensionID", entry.getKey());
            compound.setTag("worldInfo", entry.getValue().cloneNBTCompound(null));

            nbtList.appendTag(compound);
        }

        nbt.setTag("instanceInfo", nbtList);

        return nbt;
    }
}
