package com.fantasticsource.instances.world;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.network.Network;
import com.fantasticsource.instances.network.messages.SyncInstancesPacket;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.dimensions.libraryofworlds.VisitablePlayersData;
import com.fantasticsource.tools.datastructures.Pair;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
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
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class InstanceHandler extends WorldSavedData
{
    private static final String NAME = "InstanceHandler";
    public static LinkedHashMap<Integer, InstanceWorldInfo> instanceInfo = new LinkedHashMap<>();
    public static LinkedHashMap<UUID, VisitablePlayersData> visitablePlayersData = new LinkedHashMap<>();
    private static InstanceHandler instanceHandler = null;

    public InstanceHandler(String name)
    {
        super(name);
    }

    public InstanceHandler()
    {
        this(NAME);
    }

    public static void init()
    {
        //Happens on server start event

        if (instanceHandler != null) return;


        instanceInfo.clear();
        visitablePlayersData.clear();


        instanceHandler = (InstanceHandler) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage().getOrLoadData(InstanceHandler.class, NAME);

        if (instanceHandler == null)
        {
            instanceHandler = new InstanceHandler();
            FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage().setData(NAME, instanceHandler);
        }

        for (Entry<Integer, InstanceWorldInfo> entry : instanceInfo.entrySet())
        {
            int dimensionID = entry.getKey();
            InstanceWorldInfo worldInfo = entry.getValue();

            DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());
            DimensionManager.keepDimensionLoaded(dimensionID, false);

            UUID owner = worldInfo.getOwner();
            for (UUID id : worldInfo.visitorWhitelist)
            {
                visitablePlayersData.computeIfAbsent(id, o -> new VisitablePlayersData()).add(owner);
            }
        }
    }

    public static void unloadHandler()
    {
        instanceHandler = null;

        for (Map.Entry<Integer, InstanceWorldInfo> entry : instanceInfo.entrySet())
        {
            int id = entry.getKey();
            DimensionManager.unregisterDimension(id);
        }
        instanceInfo.clear();
    }

    public static void createInstance(EntityPlayerMP playerEntity, InstanceWorldInfo worldInfo)
    {
        int dimensionID = Instances.nextFreeDimID();
        instanceInfo.put(dimensionID, worldInfo);

        DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());
        loadDimension(dimensionID, worldInfo);

        playerEntity.sendMessage(new TextComponentString(String.format("Created %s using id %s", worldInfo.getWorldName(), dimensionID)).setStyle(new Style().setColor(TextFormatting.GREEN)));

        Network.WRAPPER.sendToAll(new SyncInstancesPacket());
    }

    public static Pair<Integer, InstanceWorldInfo> createInstance(ICommandSender sender, DimensionType type, UUID owner, String name)
    {
        name = name.replaceAll(" ", "_");

        int dimensionID = Instances.nextFreeDimID();

        WorldInfo tempInfo = DimensionManager.getWorld(0).getWorldInfo();
        InstanceWorldInfo worldInfo = new InstanceWorldInfo(new WorldSettings(new Random().nextLong(), GameType.CREATIVE, true, false, tempInfo.getTerrainType()), name, type);
        worldInfo.setOwner(owner);

        instanceInfo.put(dimensionID, worldInfo);

        DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());
        loadDimension(dimensionID, worldInfo);

        if (sender != null) sender.sendMessage(new TextComponentString(String.format("Created %s using id %s", worldInfo.getWorldName(), dimensionID)).setStyle(new Style().setColor(TextFormatting.GREEN)));

        Network.WRAPPER.sendToAll(new SyncInstancesPacket());

        return new Pair<>(dimensionID, worldInfo);
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

    public static void deleteInstance(ICommandSender sender, int dimensionID)
    {
        World world = DimensionManager.getWorld(dimensionID);
        if (world == null)
        {
            loadDimension(dimensionID, InstanceHandler.get(dimensionID));
            world = DimensionManager.getWorld(dimensionID);
        }

        File file = new File(world.getSaveHandler().getWorldDirectory().getAbsolutePath() + File.separator + world.provider.getSaveFolder());


        for (Entity entity : world.loadedEntityList.toArray(new Entity[0])) Teleport.escape(entity);

        ((WorldServer) world).flush();
        DimensionManager.setWorld(dimensionID, null, FMLCommonHandler.instance().getMinecraftServerInstance());
        DimensionManager.unregisterDimension(dimensionID);

        instanceInfo.remove(dimensionID);


        try
        {
            FileUtils.deleteDirectory(file);
            if (sender != null) sender.sendMessage(new TextComponentString("Completely deleted dimension " + dimensionID).setStyle(new Style().setColor(TextFormatting.GREEN)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            if (sender != null) sender.sendMessage(new TextComponentString("Error deleting dimension folder of " + dimensionID + ". Has to be removed manually.").setStyle(new Style().setColor(TextFormatting.RED)));
        }

        Network.WRAPPER.sendToAll(new SyncInstancesPacket());
    }

    public static ArrayList<String> list()
    {
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<Integer, InstanceWorldInfo> entry : instanceInfo.entrySet())
        {
            InstanceWorldInfo info = entry.getValue();
            result.add(entry.getKey() + " (" + info.getWorldName() + ") Type = " + info.getDimensionType());
        }
        return result;
    }

    public static InstanceWorldInfo get(int id)
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

            instanceInfo.put(compound.getInteger("dimensionID"), new InstanceWorldInfo(compound.getCompoundTag("worldInfo")));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        NBTTagList nbtList = new NBTTagList();

        for (Entry<Integer, InstanceWorldInfo> entry : instanceInfo.entrySet())
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
