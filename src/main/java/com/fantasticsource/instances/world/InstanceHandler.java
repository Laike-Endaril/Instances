package com.fantasticsource.instances.world;

import com.fantasticsource.instances.InstanceData;
import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.tags.savefile.Owners;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

public class InstanceHandler
{
    public static void unload(InstanceWorldInfo info)
    {
        int dim = info.world.provider.getDimension();
        info.world = null;

        if (DimensionManager.isDimensionRegistered(dim))
        {
            DimensionManager.unregisterDimension(dim);
            System.out.println(TextFormatting.GREEN + "Unregistered dimension: " + dim + " (" + info.getWorldName() + ")");
        }
    }


    public static Pair<Integer, InstanceWorldInfo> copyInstance(ICommandSender sender, String oldName, String newName)
    {
        InstanceData data = InstanceData.get(newName);
        if (data == null) return null;


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


        Pair<Integer, InstanceWorldInfo> result = loadOrCreateInstance(sender, newName);
        if (result == null) return null;

        unload(result.getValue());
        return result;
    }


    public static Pair<Integer, InstanceWorldInfo> loadOrCreateInstance(ICommandSender sender, String fullName)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) throw new RuntimeException("Cannot Hotload Dim: Server is not running!");

        WorldServer overworld = server.worlds[0];
        if (overworld == null) throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!");

        InstanceData data = InstanceData.get(fullName);
        if (data == null) throw new RuntimeException("Cannot Hotload Dim: Invalid name: " + fullName);


        ISaveHandler savehandler = overworld.getSaveHandler();

        int dimensionID = Instances.nextFreeDimID();
        DimensionManager.registerDimension(dimensionID, data.getDimensionType());

        InstanceWorldInfo worldInfo = new InstanceWorldInfo(fullName, new WorldSettings(overworld.getWorldInfo()));
        WorldInstance world = new WorldInstance(worldInfo, server, savehandler, dimensionID, overworld, server.profiler).init();
        worldInfo.world = world;
        world.addEventListener(new ServerWorldEventHandler(server, world));
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));

        if (!server.isSinglePlayer())
        {
            world.getWorldInfo().setGameType(server.getGameType());
        }

        System.out.println(TextFormatting.GREEN + "Created or loaded " + worldInfo.getWorldName() + " using id " + dimensionID);
        if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Created or loaded " + worldInfo.getWorldName()));

        return new Pair<>(dimensionID, worldInfo);
    }


    public static void load(InstanceWorldInfo info)
    {
    }


    /**
     * For loaded or unloaded instances
     */
    public static void delete(ICommandSender sender, String folderName)
    {
        String folderName2 = "Instances" + File.separator + folderName;

        for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds)
        {
            if (folderName2.equals(MCTools.getSaveFolder(world.provider).replace("Instances" + File.separator, "")))
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
        WorldServer world = info.world;
        if (world != null)
        {
            for (Entity entity : world.loadedEntityList.toArray(new Entity[0])) Teleport.escape(entity);

            world.flush();
            DimensionManager.setWorld(world.provider.getDimension(), null, FMLCommonHandler.instance().getMinecraftServerInstance());
        }


        unload(info);


        delete(sender, info.getWorldName(), true);
    }

    /**
     * For unloaded instances
     */
    protected static void delete(ICommandSender sender, String fullName, boolean internal)
    {
        File file = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + fullName);
        if (!file.exists())
        {
            System.err.println(TextFormatting.RED + "Could not find file: " + fullName);
            if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find instance to delete: " + fullName));
            return;
        }

        if (Tools.deleteFilesRecursively(file))
        {
            System.err.println(TextFormatting.GREEN + "Deleted file: " + file.getAbsolutePath());
            if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Completely deleted dimension " + fullName));
        }
        else
        {
            ServerTickTimer.schedule(1, () ->
            {
                if (Tools.deleteFilesRecursively(file))
                {
                    System.err.println(TextFormatting.GREEN + "Deleted file: " + file.getAbsolutePath());
                    if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Completely deleted dimension " + fullName));
                }
                else
                {
                    System.err.println(TextFormatting.RED + "Error deleting file: " + file.getAbsolutePath());

                    System.err.println(TextFormatting.RED + "Error deleting dimension folder for " + fullName + ". Has to be removed manually.");
                    if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error deleting dimension folder for " + fullName + ". Has to be removed manually."));
                }
            });
        }

        Owners.setNoOwner(FMLCommonHandler.instance().getMinecraftServerInstance(), fullName);
    }


    public static String getInstancesDir(MinecraftServer server)
    {
        return MCTools.getWorldSaveDir(server) + "instances" + File.separator;
    }


    public static String getDimensionTypeDir(MinecraftServer server, DimensionType instanceType)
    {
        return getInstancesDir(server) + instanceType.getName().replaceAll(" ", "_") + File.separator;
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
                for (File dimensionTypeFolder : instanceTypeFolders)
                {
                    File[] instanceFolders = dimensionTypeFolder.listFiles();
                    if (instanceFolders == null) continue;

                    for (File instanceFolder : instanceFolders) result.add(withPath ? dimensionTypeFolder.getName() + File.separator + instanceFolder.getName() : instanceFolder.getName());
                }
            }
        }

        return result;
    }

    public static ArrayList<String> instanceFolderNames(DimensionType dimensionType, boolean withPath)
    {
        ArrayList<String> result = new ArrayList<>();

        File dimensionTypeFolder = new File(getDimensionTypeDir(FMLCommonHandler.instance().getMinecraftServerInstance(), dimensionType));
        if (!dimensionTypeFolder.isDirectory()) return result;

        File[] instanceFolders = dimensionTypeFolder.listFiles();
        if (instanceFolders == null) return result;

        if (withPath) for (File instanceFolder : instanceFolders) result.add(dimensionTypeFolder.getName() + File.separator + instanceFolder.getName());
        else for (File instanceFolder : instanceFolders) result.add(instanceFolder.getName());

        return result;
    }
}
