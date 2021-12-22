package com.fantasticsource.instances.world;

import com.fantasticsource.instances.InstanceData;
import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.tags.savefile.Owners;
import com.fantasticsource.instances.tags.savefile.Visitors;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.ServerTickTimer;
import com.fantasticsource.tools.Tools;
import com.fantasticsource.tools.datastructures.Pair;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class InstanceHandler
{
    protected static boolean deleting = false;

    public static void unload(InstanceWorldInfo info)
    {
        WorldServer world = info.world;
        InstanceData data = InstanceData.get(info.getWorldName());


        if (world != null)
        {
            for (Entity entity : world.loadedEntityList.toArray(new Entity[0])) Teleport.escape(entity);

            world.flush();

            int dim = world.provider.getDimension();
            DimensionManager.setWorld(world.provider.getDimension(), null, FMLCommonHandler.instance().getMinecraftServerInstance());
            if (DimensionManager.isDimensionRegistered(dim)) DimensionManager.unregisterDimension(dim);

            info.world = null;
        }


        if (data != null && !data.saves()) delete(null, info.getWorldName(), true);
    }


    public static Pair<Integer, InstanceWorldInfo> copyInstance(ICommandSender sender, String oldName, String newName)
    {
        Profiler profiler = FMLCommonHandler.instance().getMinecraftServerInstance().profiler;
        profiler.startSection("copyInstance");

        InstanceData data = InstanceData.get(newName);
        if (data == null)
        {
            profiler.endSection();
            return null;
        }


        File oldFile = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + oldName);
        if (!oldFile.isDirectory())
        {
            profiler.endSection();
            return null;
        }


        File newFile = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + newName);
        if (newFile.exists())
        {
            if (!newFile.isDirectory())
            {
                System.err.println(TextFormatting.RED + "Failed to copy: " + newFile.getAbsolutePath() + " already exists as a non-folder");
                if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to copy to " + newName + " (already exists)"));

                profiler.endSection();
                return null;
            }

            File[] files = newFile.listFiles();
            if (files != null && files.length > 0)
            {
                System.err.println(TextFormatting.RED + "Failed to copy: " + newFile.getAbsolutePath() + " already exists as a non-empty folder");
                if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to copy to " + newName + " (already exists)"));
            }
        }
        try
        {
            FileUtils.copyDirectory(oldFile, newFile);
        }
        catch (IOException e)
        {
            System.err.println(TextFormatting.RED + "Failed to copy to directory: " + newFile.getAbsolutePath());
            if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to copy to " + newName));

            profiler.endSection();
            return null;
        }


        Pair<Integer, InstanceWorldInfo> result = loadOrCreateInstance(sender, newName);
        profiler.endSection();
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


        Profiler profiler = server.profiler;
        profiler.startSection("loadOrCreateInstance");


        ISaveHandler savehandler = overworld.getSaveHandler();

        int dimensionID = Instances.nextFreeDimID();
        DimensionManager.registerDimension(dimensionID, data.getDimensionType());

        InstanceWorldInfo worldInfo = new InstanceWorldInfo(fullName, new WorldSettings(overworld.getWorldInfo()));
        WorldInstance world = new WorldInstance(worldInfo, server, savehandler, dimensionID, overworld, server.profiler).init();
        worldInfo.world = world;
        worldInfo.init();
        world.addEventListener(new ServerWorldEventHandler(server, world));
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));

        if (!server.isSinglePlayer())
        {
            world.getWorldInfo().setGameType(server.getGameType());
        }

        if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Created or loaded " + worldInfo.getWorldName()));


        profiler.endSection();
        return new Pair<>(dimensionID, worldInfo);
    }


    /**
     * For loaded or unloaded instances
     */
    public static void delete(ICommandSender sender, String folderName)
    {
        String folderName2 = "Instances" + File.separator + folderName;

        for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds)
        {
            if (folderName2.equals(MCTools.getSaveFolder(world.provider)))
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
        if (deleting) return;

        deleting = true;
        for (Chunk chunk : info.world.getChunkProvider().getLoadedChunks()) chunk.onUnload();
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(info.world));
        delete(sender, info.getWorldName(), true);
        deleting = false;
    }

    /**
     * For unloaded instances
     */
    protected static void delete(ICommandSender sender, String fullName, boolean internal)
    {
        File file = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + fullName);
        if (!file.exists()) return;

        if (Tools.deleteFilesRecursively(file))
        {
            if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Completely deleted dimension " + fullName));
        }
        else
        {
            ServerTickTimer.schedule(1, () ->
            {
                if (Tools.deleteFilesRecursively(file))
                {
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

        Owners.setOwner(fullName, null);
        Visitors.clearValidVisitors(fullName);
    }


    public static String getInstancesDir(MinecraftServer server)
    {
        return MCTools.getWorldSaveDir(server) + "Instances" + File.separator;
    }


    public static ArrayList<String> instanceFolderNames(boolean withPath)
    {
        ArrayList<String> result = new ArrayList<>();

        result.addAll(instanceFolderNames(true, withPath));
        result.addAll(instanceFolderNames(false, withPath));

        return result;
    }

    public static ArrayList<String> instanceFolderNames(boolean saves, boolean withPath)
    {
        ArrayList<String> result = new ArrayList<>();

        File saveTypeFolder = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + (saves ? "Saved" : "Temporary"));
        if (!saveTypeFolder.isDirectory()) return result;

        File[] instanceTypeFolders = saveTypeFolder.listFiles();
        if (instanceTypeFolders == null) return result;

        for (File instanceTypeFolder : instanceTypeFolders)
        {
            DimensionType dimensionType = MCTools.getDimensionType(instanceTypeFolder.getName());
            if (dimensionType != null) result.addAll(instanceFolderNames(saves, dimensionType, withPath));
        }

        return result;
    }

    public static ArrayList<String> instanceFolderNames(boolean saves, DimensionType dimensionType, boolean withPath)
    {
        ArrayList<String> result = new ArrayList<>();

        String saveString = saves ? "Saved" : "Temporary";
        File dimensionTypeFolder = new File(getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + saveString + File.separator + dimensionType.getName());
        if (!dimensionTypeFolder.isDirectory()) return result;

        File[] instanceFolders = dimensionTypeFolder.listFiles();
        if (instanceFolders == null) return result;

        if (withPath) for (File instanceFolder : instanceFolders) result.add(saveString + File.separator + dimensionTypeFolder.getName() + File.separator + instanceFolder.getName());
        else for (File instanceFolder : instanceFolders) result.add(instanceFolder.getName());

        return result;
    }
}
