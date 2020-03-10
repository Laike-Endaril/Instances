package com.fantasticsource.instances.world;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.network.Network;
import com.fantasticsource.instances.network.messages.SyncInstancesPacket;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.instances.world.dimensions.libraryofworlds.VisitablePlayersData;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.PlayerData;
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

import java.io.*;
import java.util.*;

public class InstanceHandler
{
    private static final int INSTANCE_SAVE_FORMAT = 0;

    public static LinkedHashMap<Integer, InstanceWorldInfo> instanceInfo = new LinkedHashMap<>();
    public static LinkedHashMap<UUID, VisitablePlayersData> visitablePlayersData = new LinkedHashMap<>();


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


        File f = new File(info.SAVE_FOLDER_NAME);
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

            for (UUID id : info.visitorWhitelist)
            {
                writer.write(id + "\r\n");
            }

            writer.close();
        }
        catch (IOException e)
        {
            MCTools.crash(e, 2000, false);
        }
    }

    public static void init(FMLServerStartingEvent event) throws IOException
    {
        clear();


        File instancesFolder = new File(getInstancesDir(event.getServer()));
        if (!instancesFolder.isDirectory()) return;

        for (DimensionType instanceType : InstanceTypes.instanceTypes)
        {
            File typeFolder = new File(instancesFolder.getAbsolutePath() + File.separator + instanceType.getName().replaceAll(" ", "_"));
            if (!typeFolder.isDirectory()) continue;

            File[] instanceFolders = typeFolder.listFiles();
            if (instanceFolders == null) continue;

            for (File instanceFolder : instanceFolders)
            {
                if (!instanceFolder.isDirectory()) continue;

                File instanceFile = new File(instanceFolder.getAbsolutePath() + File.separator + "instanceData.txt");
                if (!instanceFile.exists() || instanceFile.isDirectory()) continue;


                BufferedReader reader = new BufferedReader(new FileReader(instanceFile));
                try
                {
                    switch (Integer.parseInt(reader.readLine())) //Instance world data format version
                    {
                        case 0:
                            UUID owner = null;
                            String s = reader.readLine();
                            if (s != null)
                            {
                                s = s.trim();
                                if (!s.equals(""))
                                {
                                    try
                                    {
                                        owner = UUID.fromString(s);
                                    }
                                    catch (IllegalArgumentException e)
                                    {
                                        //Leave set to null
                                    }
                                }
                            }

                            //TODO This code doesn't seem to have been necessary, but I'm leaving it commented out here for now just in case
//                            if (instanceType == InstanceTypes.skyroomDimType)
//                            {
//                                String playername = PlayerData.getName(owner);
//                                createInstance(null, instanceType, owner, playername != null ? playername + "'s " + InstanceTypes.skyroomDimType.getName() : instanceFolder.getName(), false);
//                            }
//                            else
//                            {
//                                throw new IllegalStateException("Found unknown file: " + instanceFile);
//                            }

                            s = reader.readLine();
                            while (s != null)
                            {
                                try
                                {
                                    UUID id = UUID.fromString(s.trim());
                                    visitablePlayersData.computeIfAbsent(id, o -> new VisitablePlayersData()).add(owner);
                                }
                                catch (IllegalArgumentException e)
                                {
                                }

                                s = reader.readLine();
                            }

                            break;


                        default:
                            throw new IllegalArgumentException("Unknown instance world data format!  Skipping: " + instanceFile.getName());
                    }
                }
                catch (IllegalArgumentException e) //NumberFormatException is a subclass
                {
                    reader.close();
                    continue;
                }
            }
        }
    }

    public static void clear()
    {
        for (Map.Entry<Integer, InstanceWorldInfo> entry : instanceInfo.entrySet())
        {
            unload(entry.getValue());
            DimensionManager.unregisterDimension(entry.getKey());
        }

        instanceInfo.clear();
        visitablePlayersData.clear();
    }


    public static Pair<Integer, InstanceWorldInfo> createInstance(ICommandSender sender, DimensionType dimType, UUID owner, boolean save)
    {
        PlayerData data = PlayerData.get(owner);
        Entity ownerEntity = data == null ? null : data.player;

        return createInstance(sender, dimType, owner, ownerEntity == null ? dimType.name() : ownerEntity.getName() + "'s " + dimType.getName(), save);
    }

    public static Pair<Integer, InstanceWorldInfo> createInstance(ICommandSender sender, DimensionType dimType, UUID owner, String name, boolean save)
    {
        name = name.replaceAll(" ", "_");

        int dimensionID = Instances.nextFreeDimID();

        WorldType worldType = DimensionManager.getWorld(0).getWorldInfo().getTerrainType();
        WorldSettings settings = new WorldSettings(new Random().nextLong(), GameType.CREATIVE, true, false, worldType);
        InstanceWorldInfo worldInfo = new InstanceWorldInfo(dimensionID, settings, owner, name, dimType, save);

        instanceInfo.put(dimensionID, worldInfo);

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
        DimensionManager.unregisterDimension(dimensionID);

        instanceInfo.remove(dimensionID);


        File file = new File(info.SAVE_FOLDER_NAME);
        if (Tools.deleteFilesRecursively(file))
        {
            if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Completely deleted dimension " + dimensionID));
        }
        else
        {
            if (sender != null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error deleting dimension folder of " + dimensionID + ". Has to be removed manually."));
            else System.err.println(TextFormatting.RED + "Error deleting dimension folder of " + dimensionID + ". Has to be removed manually.");
        }

        Network.WRAPPER.sendToAll(new SyncInstancesPacket());
    }

    public static ArrayList<String> list()
    {
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<Integer, InstanceWorldInfo> entry : instanceInfo.entrySet())
        {
            InstanceWorldInfo info = entry.getValue();
            result.add(entry.getKey() + " (" + info.getWorldName() + ")");
        }
        return result;
    }

    public static InstanceWorldInfo get(int instDimID)
    {
        return instanceInfo.get(instDimID);
    }

    public static String getInstancesDir(MinecraftServer server)
    {
        return MCTools.getDataDir(server) + ".." + File.separator + "instances" + File.separator;
    }
}
