package com.fantasticsource.instances.world;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.network.Network;
import com.fantasticsource.instances.network.messages.SyncInstancesPacket;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.tags.savefile.Owners;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.ServerTickTimer;
import com.fantasticsource.tools.Tools;
import com.fantasticsource.tools.datastructures.Pair;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.UUID;

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

        loadedInstances.remove(info.dimensionID);
        if (DimensionManager.isDimensionRegistered(info.dimensionID))
        {
            DimensionManager.unregisterDimension(info.dimensionID);
            System.out.println(TextFormatting.GREEN + "Unregistered dimension: " + info.dimensionID + " (" + info.getWorldName() + ")");

            Network.WRAPPER.sendToAll(new SyncInstancesPacket()); //TODO change this to use direct data from dimension manager
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

    public static void clear()
    {
        for (InstanceWorldInfo info : loadedInstances.values()) unload(info);
    }


    public static Pair<Integer, InstanceWorldInfo> copyInstance(ICommandSender sender, String oldName, String newName, DimensionType instanceType, UUID owner, boolean save)
    {
        if (!Tools.contains(InstanceTypes.instanceTypes, instanceType)) return null;


        File oldFile = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + oldName);
        if (!oldFile.isDirectory()) return null;


        File newFile = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + newName);
        if (newFile.exists())
        {
            if (!newFile.isDirectory())
            {
                System.err.println(TextFormatting.RED + "Failed to copy: " + newFile.getAbsolutePath() + " already exists as a non-folder");
                if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Failed to copy to " + newName + " (already exists)"));
                return null;
            }

            File[] files = newFile.listFiles();
            if (files != null && files.length > 0)
            {
                System.err.println(TextFormatting.RED + "Failed to copy: " + newFile.getAbsolutePath() + " already exists as a non-empty folder");
                if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Failed to copy to " + newName + " (already exists)"));
            }
        }
        try
        {
            FileUtils.copyDirectory(oldFile, newFile);
        }
        catch (IOException e)
        {
            System.err.println(TextFormatting.RED + "Failed to copy to directory: " + newFile.getAbsolutePath());
            if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Failed to copy to " + newName));
            return null;
        }


        Pair<Integer, InstanceWorldInfo> result = loadOrCreateInstance(sender, instanceType, owner, newName, save);
        if (result == null) return null;

        unload(result.getValue());
        return result;
    }


    public static Pair<Integer, InstanceWorldInfo> loadOrCreateInstance(ICommandSender sender, DimensionType instanceType, UUID owner, String name, boolean save)
    {
        if (!Tools.contains(InstanceTypes.instanceTypes, instanceType)) return null;


        name = name.replaceAll(" ", "_");

        int dimensionID = Instances.nextFreeDimID();

        WorldType worldType = DimensionManager.getWorld(0).getWorldInfo().getTerrainType();
        WorldSettings settings = new WorldSettings(new Random().nextLong(), GameType.CREATIVE, true, false, worldType);
        InstanceWorldInfo worldInfo = new InstanceWorldInfo(dimensionID, settings, owner, name, instanceType, save);

        loadedInstances.put(dimensionID, worldInfo);

        DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());

        System.out.println(TextFormatting.GREEN + "Created or loaded " + worldInfo.getWorldName() + " using id " + dimensionID);
        if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Created or loaded " + worldInfo.getWorldName()));

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
    public static void delete(ICommandSender sender, String folderName)
    {
        String folderName2 = "instances" + File.separator + folderName;

        for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds)
        {
            if (folderName2.equals(world.provider.getSaveFolder()))
            {
                delete(sender, world);
                return;
            }
        }

        delete(sender, folderName, true);
    }

    /**
     * For loaded instances
     */
    public static void delete(ICommandSender sender, World world)
    {
        if (!(world instanceof WorldServer) || !(world.getWorldInfo() instanceof InstanceWorldInfo)) return;

        delete(sender, (InstanceWorldInfo) world.getWorldInfo());
    }

    /**
     * For loaded instances
     */
    public static void delete(ICommandSender sender, InstanceWorldInfo info)
    {
        int dimensionID = info.dimensionID;
        WorldServer world = info.world;
        if (world != null)
        {
            for (Entity entity : world.loadedEntityList.toArray(new Entity[0])) Teleport.escape(entity);

            world.flush();
            DimensionManager.setWorld(dimensionID, null, FMLCommonHandler.instance().getMinecraftServerInstance());
        }


        unload(info);


        delete(sender, info.saveFolderName, true);
    }

    /**
     * For unloaded instances
     */
    protected static void delete(ICommandSender sender, String folderName, boolean internal)
    {
        File file = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + folderName);
        if (!file.exists())
        {
            System.err.println(TextFormatting.RED + "Could not find file: " + folderName);
            if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find instance to delete: " + folderName));
            return;
        }

        if (Tools.deleteFilesRecursively(file))
        {
            System.err.println(TextFormatting.GREEN + "Deleted file: " + file.getAbsolutePath());
            if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Completely deleted dimension " + folderName));
        }
        else
        {
            ServerTickTimer.schedule(1, () ->
            {
                if (Tools.deleteFilesRecursively(file))
                {
                    System.err.println(TextFormatting.GREEN + "Deleted file: " + file.getAbsolutePath());
                    if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Completely deleted dimension " + folderName));
                }
                else
                {
                    System.err.println(TextFormatting.RED + "Error deleting file: " + file.getAbsolutePath());

                    System.err.println(TextFormatting.RED + "Error deleting dimension folder for " + folderName + ". Has to be removed manually.");
                    if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error deleting dimension folder for " + folderName + ". Has to be removed manually."));
                }
            });
        }

        Owners.setNoOwner(FMLCommonHandler.instance().getMinecraftServerInstance(), folderName);
    }


    public static InstanceWorldInfo get(int instDimID)
    {
        return loadedInstances.get(instDimID);
    }


    public static String getInstancesDir(MinecraftServer server)
    {
        return MCTools.getWorldSaveDir(server) + "instances" + File.separator;
    }


    public static ArrayList<String> instanceFolderNames(boolean withPath)
    {
        ArrayList<String> result = new ArrayList<>();

        File instancesFolder = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()));
        if (instancesFolder.isDirectory())
        {
            File[] instanceTypeFolders = instancesFolder.listFiles();
            if (instanceTypeFolders != null)
            {
                for (File instanceTypeFolder : instanceTypeFolders)
                {
                    File[] instanceFolders = instanceTypeFolder.listFiles();
                    if (instanceFolders == null) continue;

                    for (File instanceFolder : instanceFolders) result.add(withPath ? instanceTypeFolder.getName() + File.separator + instanceFolder.getName() : instanceFolder.getName());
                }
            }
        }

        return result;
    }

    public static ArrayList<String> instanceFolderNames(DimensionType instanceType, boolean withPath)
    {
        ArrayList<String> result = new ArrayList<>();
        if (!Tools.contains(InstanceTypes.instanceTypes, instanceType)) return result;

        File instanceTypeFolder = new File(InstanceTypes.getInstanceTypeDir(FMLCommonHandler.instance().getMinecraftServerInstance(), instanceType));
        if (!instanceTypeFolder.isDirectory()) return result;

        File[] instanceFolders = instanceTypeFolder.listFiles();
        if (instanceFolders == null) return result;

        if (withPath) for (File instanceFolder : instanceFolders) result.add(instanceTypeFolder.getName() + File.separator + instanceFolder.getName());
        else for (File instanceFolder : instanceFolders) result.add(instanceFolder.getName());

        return result;
    }
}
