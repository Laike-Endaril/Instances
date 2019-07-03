package com.fantasticsource.instances.server;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.network.PacketHandler;
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
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class InstanceHandler extends WorldSavedData
{
    private static String NAME = "InstanceHandler";
    private static HashMap<Integer, WorldInfoSimple> dimensionInfo;
    private static InstanceHandler instanceHandler = null;

    public InstanceHandler(String name)
    {
        super(name);

        dimensionInfo = new HashMap<>();
    }

    public InstanceHandler()
    {
        super(NAME);

        dimensionInfo = new HashMap<>();
    }

    public static void load()
    {
        if (instanceHandler != null) return;

        instanceHandler = (InstanceHandler) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage().getOrLoadData(InstanceHandler.class, NAME);

        if (instanceHandler == null)
        {
            instanceHandler = new InstanceHandler();
            FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage().setData(NAME, instanceHandler);
        }

        for (Entry<Integer, WorldInfoSimple> entry : dimensionInfo.entrySet())
        {
            int dimensionID = entry.getKey();
            WorldInfoSimple worldInfo = entry.getValue();

            DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());

            System.out.println(worldInfo.getDimensionType());

            loadDimension(dimensionID, worldInfo);
        }
    }

    public static void unload()
    {
        instanceHandler = null;

        for (Map.Entry<Integer, WorldInfoSimple> entry : dimensionInfo.entrySet())
        {
            int id = entry.getKey();
            System.out.println("Unregistering instance " + id + " (" + entry.getValue().getWorldName() + ")");
            DimensionManager.unregisterDimension(id);
        }
        dimensionInfo.clear();
    }

    public static void createDimension(EntityPlayerMP playerEntity, WorldInfoSimple worldInfo)
    {
        int dimensionID = Instances.nextFreeDimID();
        dimensionInfo.put(dimensionID, worldInfo);

        DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());
        loadDimension(dimensionID, worldInfo);

        playerEntity.sendMessage(new TextComponentString(String.format("Created %s using id %s", worldInfo.getWorldName(), dimensionID)).setStyle(new Style().setColor(TextFormatting.GREEN)));
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

    public static void deleteDimension(ICommandSender sender, int dimensionID)
    {
        WorldServer w = DimensionManager.getWorld(dimensionID);

        if (!dimensionInfo.containsKey(dimensionID))
        {
            if (w == null)
            {
                sender.sendMessage(new TextComponentString("No dimension with that id exists").setStyle(new Style().setColor(TextFormatting.RED)));
            }
            else
            {
                sender.sendMessage(new TextComponentString("The dimension associated with that id is not from the Instances mod").setStyle(new Style().setColor(TextFormatting.RED)));
            }
            return;
        }

        if (w == null)
        {
            if (DimensionManager.isDimensionRegistered(dimensionID))
            {
                loadDimension(dimensionID, dimensionInfo.get(dimensionID));
                w = DimensionManager.getWorld(dimensionID);
                if (w == null)
                {
                    sender.sendMessage(new TextComponentString("Failed to load dimension").setStyle(new Style().setColor(TextFormatting.RED)));
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
                    player.sendMessage(new TextComponentString("The dimension you were in was deleted").setStyle(new Style().setColor(TextFormatting.RED)));
                }
            }
        }

        MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(w));
        w.flush();
        DimensionManager.setWorld(dimensionID, null, w.getMinecraftServer());
        DimensionManager.unregisterDimension(dimensionID);

        dimensionInfo.remove(dimensionID);
        w.flush();

        File dimensionFolder = new File(DimensionManager.getCurrentSaveRootDirectory(), "DIM" + dimensionID);

        try
        {
            FileUtils.deleteDirectory(dimensionFolder);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            sender.sendMessage(new TextComponentString("Error deleting dimension folder of " + dimensionID + ". Has to be removed manually.").setStyle(new Style().setColor(TextFormatting.RED)));
        }
        finally
        {
            sender.sendMessage(new TextComponentString("Completely deleted dimension " + dimensionID).setStyle(new Style().setColor(TextFormatting.GREEN)));
        }
    }

    private static void syncWithClients()
    {
        MessageDimensionSync message = new MessageDimensionSync();

        for (Map.Entry<Integer, WorldInfoSimple> entry : dimensionInfo.entrySet())
        {
            message.addDimension(entry.getKey(), entry.getValue().getDimensionType());
        }

        PacketHandler.INSTANCE.sendToAll(message);
    }

    public static IMessage constructSyncMessage()
    {
        MessageDimensionSync message = new MessageDimensionSync();

        for (Map.Entry<Integer, WorldInfoSimple> entry : dimensionInfo.entrySet())
        {
            message.addDimension(entry.getKey(), entry.getValue().getDimensionType());
        }

        return message;
    }

    @Override
    public boolean isDirty()
    {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        NBTTagList nbtList = nbt.getTagList("dimensionInfo", 10);

        for (int i = 0; i < nbtList.tagCount(); i++)
        {
            NBTTagCompound compound = nbtList.getCompoundTagAt(i);

            dimensionInfo.put(compound.getInteger("dimensionID"), new WorldInfoSimple(compound.getCompoundTag("worldInfo")));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        NBTTagList nbtList = new NBTTagList();

        for (Entry<Integer, WorldInfoSimple> entry : dimensionInfo.entrySet())
        {
            NBTTagCompound compound = new NBTTagCompound();

            compound.setInteger("dimensionID", entry.getKey());
            compound.setTag("worldInfo", entry.getValue().cloneNBTCompound(null));

            nbtList.appendTag(compound);
        }

        nbt.setTag("dimensionInfo", nbtList);

        return nbt;
    }
}
