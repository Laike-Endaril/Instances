package com.fantasticsource.instances.world;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.network.Network;
import com.fantasticsource.instances.network.messages.SyncInstancesPacket;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.tools.Tools;
import com.fantasticsource.tools.datastructures.Pair;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class InstanceHandler
{
    private static final int INSTANCE_SAVE_FORMAT = 0;

    public static LinkedHashMap<Integer, InstanceWorldInfo> loadedInstances = new LinkedHashMap<>();


    public static void unload(InstanceWorldInfo info)
    {
        if (info.world != null)
        {
            trySave(info);
            info.world = null;
        }
    }

    public static void trySave(InstanceWorldInfo info)
    {
        if (!info.save) return;


        File f = new File(info.saveFolderName);
        if (!f.exists())
        {
            if (!f.mkdirs()) throw new IllegalStateException("Failed to create " + f);
        }

        f = new File(f.getAbsolutePath() + File.separator + "instanceData.txt");


        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));

            writer.write(INSTANCE_SAVE_FORMAT + "\r\n");

            UUID owner = info.getOwner();
            writer.write((owner == null ? "" : owner) + "\r\n");

            writer.close();
        }
        catch (IOException e)
        {
            MCTools.crash(e, 2000, false);
        }
    }

    public static void init(FMLServerStartingEvent event)
    {
        clear();
    }

    public static ArrayList<String> instanceFolderNames()
    {
        ArrayList<String> result = new ArrayList<>();

        File instancesFolder = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()));
        if (!instancesFolder.isDirectory()) return result;

        File[] instanceTypeFolders = instancesFolder.listFiles();
        if (instanceTypeFolders == null) return result;

        for (File instanceTypeFolder : instanceTypeFolders)
        {
            if (!instancesFolder.isDirectory()) continue;

            File[] instanceFolders = instanceTypeFolder.listFiles();
            if (instanceFolders == null) continue;

            for (File instanceFolder : instanceFolders) result.add(instanceTypeFolder.getName() + File.separator + instanceFolder.getName());
        }

        return result;
    }

    public static void clear()
    {
        for (Map.Entry<Integer, InstanceWorldInfo> entry : loadedInstances.entrySet())
        {
            unload(entry.getValue());
            DimensionManager.unregisterDimension(entry.getKey());
        }

        loadedInstances.clear();
    }


    public static Pair<Integer, InstanceWorldInfo> createInstance(ICommandSender sender, DimensionType dimType, UUID owner, boolean save)
    {
        return createInstance(sender, dimType, owner, "" + owner, save);
    }

    public static Pair<Integer, InstanceWorldInfo> createInstance(ICommandSender sender, DimensionType dimType, UUID owner, String name, boolean save)
    {
        name = name.replaceAll(" ", "_");

        int dimensionID = Instances.nextFreeDimID();

        WorldType worldType = DimensionManager.getWorld(0).getWorldInfo().getTerrainType();
        WorldSettings settings = new WorldSettings(new Random().nextLong(), GameType.CREATIVE, true, false, worldType);
        InstanceWorldInfo worldInfo = new InstanceWorldInfo(dimensionID, settings, owner, name, dimType, save);

        loadedInstances.put(dimensionID, worldInfo);

        DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());

        if (sender != null) sender.sendMessage(new TextComponentString(String.format("Created %s using id %s", worldInfo.getWorldName(), dimensionID)).setStyle(new Style().setColor(TextFormatting.GREEN)));

        Network.WRAPPER.sendToAll(new SyncInstancesPacket());

        return new Pair<>(dimensionID, worldInfo);
    }


    public static void load(InstanceWorldInfo info)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) throw new RuntimeException("Cannot Hotload Dim: Server is not running!");

        WorldServer overworld = (WorldServer) server.getEntityWorld();
        if (overworld == null) throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!");


        ISaveHandler savehandler = overworld.getSaveHandler();

        WorldInstance world = new WorldInstance(info, server, savehandler, info.dimensionID, overworld, server.profiler).init();
        info.world = world;
        world.addEventListener(new ServerWorldEventHandler(server, world));
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));

        if (!server.isSinglePlayer())
        {
            world.getWorldInfo().setGameType(server.getGameType());
        }
    }


    /**
     * For loaded or unloaded instances
     */
    public static boolean delete(ICommandSender sender, String folderName)
    {
        folderName = "instances" + File.separator + folderName;

        for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds)
        {
            if (folderName.equals(world.provider.getSaveFolder()))
            {
                return delete(sender, world);
            }
        }

        return delete(sender, folderName, true);
    }

    /**
     * For loaded instances
     */
    public static boolean delete(ICommandSender sender, World world)
    {
        if (!(world instanceof WorldServer) || !(world.getWorldInfo() instanceof InstanceWorldInfo)) return false;

        return delete(sender, (InstanceWorldInfo) world.getWorldInfo());
    }

    /**
     * For loaded instances
     */
    public static boolean delete(ICommandSender sender, InstanceWorldInfo info)
    {
        int dimensionID = info.dimensionID;
        WorldServer world = info.world;
        if (world != null)
        {
            for (Entity entity : world.loadedEntityList.toArray(new Entity[0])) Teleport.escape(entity);

            world.flush();
            DimensionManager.setWorld(dimensionID, null, FMLCommonHandler.instance().getMinecraftServerInstance());
        }
        DimensionManager.unregisterDimension(dimensionID);

        loadedInstances.remove(dimensionID);


        Network.WRAPPER.sendToAll(new SyncInstancesPacket());


        return delete(sender, info.saveFolderName, true);
    }

    /**
     * For unloaded instances
     */
    protected static boolean delete(ICommandSender sender, String folderName, boolean internal)
    {
        File file = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + folderName);
        if (Tools.deleteFilesRecursively(file))
        {
            System.err.println(TextFormatting.GREEN + "Deleted file: " + file.getAbsolutePath());
            if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Completely deleted dimension " + folderName));
            return true;
        }
        else
        {
            System.err.println(TextFormatting.RED + "Error deleting file: " + file.getAbsolutePath());

            System.err.println(TextFormatting.RED + "Error deleting dimension folder for " + folderName + ". Has to be removed manually.");
            if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error deleting dimension folder for " + folderName + ". Has to be removed manually."));
            return false;
        }
    }


    public static ArrayList<String> list()
    {
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<Integer, InstanceWorldInfo> entry : loadedInstances.entrySet())
        {
            InstanceWorldInfo info = entry.getValue();
            result.add(entry.getKey() + " (" + info.getWorldName() + ")");
        }
        return result;
    }

    public static InstanceWorldInfo get(int instDimID)
    {
        return loadedInstances.get(instDimID);
    }

    public static String getInstancesDir(MinecraftServer server)
    {
        return MCTools.getWorldSaveDir(server) + "instances" + File.separator;
    }
}
