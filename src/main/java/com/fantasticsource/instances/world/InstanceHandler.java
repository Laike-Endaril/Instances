package com.fantasticsource.instances.world;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.network.Network;
import com.fantasticsource.instances.network.messages.SyncInstancesPacket;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.instances.world.dimensions.libraryofworlds.VisitablePlayersData;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.tools.datastructures.Pair;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;

public class InstanceHandler
{
    private static final int INSTANCE_SAVE_FORMAT = 0;

    public static LinkedHashMap<Integer, InstanceWorldInfo> instanceInfo = new LinkedHashMap<>();
    public static LinkedHashMap<UUID, VisitablePlayersData> visitablePlayersData = new LinkedHashMap<>();


    public static void save(World world) throws IOException
    {
        WorldInfo worldInfo = world.getWorldInfo();

        if (!(worldInfo instanceof InstanceWorldInfo)) return;
        save((InstanceWorldInfo) worldInfo);
    }

    public static void save(InstanceWorldInfo info) throws IOException
    {
        File f = new File(MCTools.getDataDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + ".." + File.separator + "instances" + File.separator);
        if (!f.exists())
        {
            if (!f.mkdir()) throw new IllegalStateException("Failed to create " + f);
        }

        f = new File(f.getAbsolutePath() + File.separator + info.getDimensionType().getName().replaceAll(" ", "_"));
        if (!f.exists())
        {
            if (!f.mkdir()) throw new IllegalStateException("Failed to create " + f);
        }

        f = new File(f.getAbsolutePath() + File.separator + info.getWorldName());
        if (!f.exists())
        {
            if (!f.mkdir()) throw new IllegalStateException("Failed to create " + f);
        }

        f = new File(f.getAbsolutePath() + File.separator + "data.txt");


        BufferedWriter writer = new BufferedWriter(new FileWriter(f));

        writer.write(INSTANCE_SAVE_FORMAT + "\r\n");

        UUID owner = info.getOwner();
        writer.write((owner == null ? "" : owner) + "\r\n");

        for (UUID id : info.visitorWhitelist)
        {
            writer.write(id + "\r\n");
        }

        writer.close();

        System.out.println("Saved " + info.getWorldName());
    }

    public static void load(FMLServerStartingEvent event) throws IOException
    {
        clear();


        File instancesFolder = new File(MCTools.getDataDir(event.getServer()) + ".." + File.separator + "instances" + File.separator);
        if (!instancesFolder.isDirectory()) return;

        for (DimensionType instanceType : InstanceTypes.dimensionTypes)
        {
            File typeFolder = new File(instancesFolder.getAbsolutePath() + File.separator + instanceType.getName().replaceAll(" ", "_"));
            if (!typeFolder.isDirectory()) continue;

            File[] instanceFolders = typeFolder.listFiles();
            if (instanceFolders == null) continue;

            for (File instanceFolder : instanceFolders)
            {
                if (!instanceFolder.isDirectory()) continue;

                File instanceFile = new File(instanceFolder.getAbsolutePath() + File.separator + "data.txt");
                if (!instanceFile.exists() || instanceFile.isDirectory()) continue;


                BufferedReader reader = new BufferedReader(new FileReader(instanceFile));
                try
                {
                    switch (Integer.parseInt(reader.readLine()))
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

                            createInstance(null, instanceType, owner, instanceFolder.getName(), false);

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

    private static void clear()
    {
        for (Map.Entry<Integer, InstanceWorldInfo> entry : instanceInfo.entrySet())
        {
            int id = entry.getKey();
            DimensionManager.unregisterDimension(id);
        }

        instanceInfo.clear();
        visitablePlayersData.clear();
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

    public static Pair<Integer, InstanceWorldInfo> createInstance(ICommandSender sender, DimensionType dimType, UUID owner, String name, boolean save)
    {
        name = name.replaceAll(" ", "_");

        int dimensionID = Instances.nextFreeDimID();

        WorldType worldType = DimensionManager.getWorld(0).getWorldInfo().getTerrainType();
        WorldSettings settings = new WorldSettings(new Random().nextLong(), GameType.CREATIVE, true, false, worldType);
        InstanceWorldInfo worldInfo = new InstanceWorldInfo(settings, name, dimType);
        worldInfo.setOwner(owner);
        if (save)
        {
            try
            {
                save(worldInfo);
            }
            catch (Exception e)
            {
                MCTools.crash(e, 2000, false);
            }
        }

        instanceInfo.put(dimensionID, worldInfo);

        DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());
        loadDimension(dimensionID, worldInfo);

        if (sender != null) sender.sendMessage(new TextComponentString(String.format("Created %s using id %s", worldInfo.getWorldName(), dimensionID)).setStyle(new Style().setColor(TextFormatting.GREEN)));

        Network.WRAPPER.sendToAll(new SyncInstancesPacket());

        return new Pair<>(dimensionID, worldInfo);
    }

    private static void loadDimension(int dimensionID, WorldInfo worldInfo)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) throw new RuntimeException("Cannot Hotload Dim: Server is not running!");

        WorldServer overworld = (WorldServer) server.getEntityWorld();
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


        ISaveHandler savehandler = overworld.getSaveHandler();
        EnumDifficulty difficulty = overworld.getDifficulty();

        WorldServer world = (WorldServer) (new WorldCustom(worldInfo, server, savehandler, dimensionID, overworld, server.profiler).init());
        world.addEventListener(new ServerWorldEventHandler(server, world));
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));

        if (!server.isSinglePlayer())
        {
            world.getWorldInfo().setGameType(server.getGameType());
        }

        server.setDifficultyForAllWorlds(difficulty);
    }

    public static void deleteInstance(ICommandSender sender, int dimensionID)
    {
        WorldServer world = DimensionManager.getWorld(dimensionID);
        if (world == null)
        {
            loadDimension(dimensionID, InstanceHandler.get(dimensionID));
            world = DimensionManager.getWorld(dimensionID);
        }

        File file = new File(world.getSaveHandler().getWorldDirectory().getAbsolutePath() + File.separator + world.provider.getSaveFolder());


        for (Entity entity : world.loadedEntityList.toArray(new Entity[0])) Teleport.escape(entity);

        world.flush();
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

    public static InstanceWorldInfo get(int instDimID)
    {
        return instanceInfo.get(instDimID);
    }
}
