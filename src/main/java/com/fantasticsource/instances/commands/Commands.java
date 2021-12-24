package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.InstanceData;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.tags.savefile.Owners;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.*;
import com.fantasticsource.tools.Tools;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.*;
import java.util.*;

public class Commands extends CommandBase
{
    protected static HashMap<World, ConnectionData> pendingConnectionData = new HashMap<>();

    public static ArrayList<String> playernames()
    {
        ArrayList<String> strings = new ArrayList<>();

        for (PlayerData data : PlayerData.playerData.values())
        {
            strings.add(data.name);
        }

        return strings;
    }

    @Override
    public String getName()
    {
        return "instances";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "Usage: /instances <library:skyroom:template:list:delete:copy:join:setOwner:removeOwner:joinTempCopy:roomTile:roomTags:roomConnections>";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "library", "skyroom", "template", "list", "delete", "copy", "join", "setOwner", "joinTempCopy", "roomTile", "roomTags", "roomConnections");
        }
        else if (args.length == 2)
        {
            if (args[0].equals("delete") || args[0].equals("copy") || args[0].equals("join") || args[0].equals("setOwner") || args[0].equals("joinTempCopy") || args[0].equals("roomTile"))
            {
                return getListOfStringsMatchingLastWord(args, InstanceHandler.instanceFolderNames(true));
            }
            else if (args[0].equals("skyroom"))
            {
                return getListOfStringsMatchingLastWord(args, playernames());
            }
            else if (args[0].equals("template"))
            {
                return getListOfStringsMatchingLastWord(args, InstanceHandler.instanceFolderNames(true, InstanceTypes.TEMPLATE, false));
            }
            else if (args[0].equals("roomTags"))
            {
                return getListOfStringsMatchingLastWord(args, "add", "remove");
            }
            else if (args[0].equals("roomConnections"))
            {
                return getListOfStringsMatchingLastWord(args, "add", "remove", "cancel", "mode");
            }
        }
        else if (args.length == 3)
        {
            if (args[0].equals("join") || args[0].equals("setOwner") || args[0].equals("joinTempCopy") || args[0].equals("roomTile"))
            {
                return getListOfStringsMatchingLastWord(args, playernames());
            }
            else if (args[0].equals("roomConnections") && args[1].equals("mode"))
            {
                return getListOfStringsMatchingLastWord(args, "twoWay", "exitOnly", "entranceOnly");
            }
        }
        else if (args.length == 4)
        {
            if (args[0].equals("copy"))
            {
                return getListOfStringsMatchingLastWord(args, playernames());
            }
        }

        return new ArrayList<>();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            sender.sendMessage(new TextComponentString(getUsage(sender)));
            return;
        }

        EntityPlayerMP player = null;
        InstanceData data;
        switch (args[0])
        {
            case "roomConnections":
                if (!(sender instanceof EntityPlayerMP)) sender.sendMessage(new TextComponentString(TextFormatting.RED + "This command is only usable by players"));
                else if (args.length < 2) sender.sendMessage(new TextComponentString(getUsage(sender)));
                else
                {
                    player = (EntityPlayerMP) sender;
                    DimensionType type = player.world.provider.getDimensionType();
                    if (type != InstanceTypes.ROOM_TILE) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Room connection command is only available in room tiles"));
                    else
                    {
                        World world = player.world;
                        ConnectionData connectionData = pendingConnectionData.get(world);
                        if (args[1].equals("add"))
                        {
                            if (player.world.playerEntities.size() > 1) player.sendMessage(new TextComponentString(TextFormatting.RED + "To prevent conflicts, only one player may be in the room tile instance when editing connections"));
                            else
                            {
                                data = InstanceData.get(world);
                                if (connectionData == null || !connectionData.instanceData.getFullName().equals(data.getFullName()))
                                {
                                    connectionData = new ConnectionData(player);
                                    File file = new File(MCTools.getWorldSaveDir(server) + MCTools.getSaveFolder(player.world.provider) + File.separator + "Connect " + connectionData.facing + " " + connectionData.pos + ".txt");
                                    if (file.exists())
                                    {
                                        //Reset pending connection status
                                        pendingConnectionData.remove(world);
                                        player.sendMessage(new TextComponentString(TextFormatting.RED + "Could not create connection at " + connectionData.pos + ": a connection already exists there!"));
                                    }
                                    else
                                    {
                                        //Start creating connection
                                        pendingConnectionData.put(world, connectionData);
                                        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Started creating connection at " + connectionData.pos + " facing " + connectionData.facing));
                                        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Edit blocks to create any terrain differences when the connection is open, then enter the same command to finish"));
                                        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "To cancel, use this command: /instances roomConnections cancel"));
                                        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "If you didn't use this while outside the room facing in, you should cancel and do that"));
                                    }
                                }
                                else
                                {
                                    pendingConnectionData.remove(world);
                                    File file = new File(MCTools.getWorldSaveDir(server) + MCTools.getSaveFolder(player.world.provider) + File.separator + "Connect " + connectionData.facing + " " + connectionData.pos + ".txt");
                                    try
                                    {
                                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                                        for (BlockPos pos : connectionData.oldBlockStates.keySet())
                                        {
                                            IBlockState blockState = world.getBlockState(pos);
                                            Block block = blockState.getBlock();
                                            writer.write(pos.getX() + "," + pos.getY() + "," + pos.getZ() + "\r\n");
                                            writer.write(block.getRegistryName() + ":" + block.getMetaFromState(blockState) + "\r\n");
                                            for (IProperty<?> property : blockState.getPropertyKeys())
                                            {
                                                writer.write(property.getName() + "\r\n");
                                                writer.write(blockState.getValue(property) + "\r\n");
                                            }
                                            TileEntity te = world.getTileEntity(pos);
                                            if (te != null) writer.write(te.serializeNBT().toString());
                                            writer.write("\r\n");
                                        }
                                        writer.close();
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }
                                    for (Map.Entry<BlockPos, IBlockState> entry : connectionData.oldBlockStates.entrySet()) world.setBlockState(entry.getKey(), entry.getValue());

                                    player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Connection saved and temporary block changes undone"));
                                }
                            }
                        }
                        else if (args[1].equals("remove"))
                        {
                            BlockPos[] blocksInRay = ImprovedRayTracing.blocksInRay(player, 200, true);
                            String stringPos = blocksInRay[blocksInRay.length - 1].toString();
                            boolean removed = false;
                            File dir = new File(MCTools.getWorldSaveDir(server) + MCTools.getSaveFolder(player.world.provider));
                            File[] files = dir.listFiles();
                            if (files != null)
                            {
                                for (File file : files)
                                {
                                    if (file.getName().equals("Connect west " + stringPos + ".txt") || file.getName().equals("Connect east " + stringPos + ".txt")
                                            || file.getName().equals("Connect north " + stringPos + ".txt") || file.getName().equals("Connect south " + stringPos + ".txt")
                                            || file.getName().equals("Connect up " + stringPos + ".txt") || file.getName().equals("Connect down " + stringPos + ".txt"))
                                    {
                                        while (file.exists()) file.delete();
                                        removed = true;
                                        break;
                                    }
                                }
                            }
                            if (removed) player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Removed connection at " + stringPos));
                            else player.sendMessage(new TextComponentString(TextFormatting.RED + "Could not remove connection at " + stringPos + ": no connection exists at this position"));
                        }
                        else if (args[1].equals("cancel"))
                        {
                            pendingConnectionData.remove(world);
                            if (connectionData != null)
                            {
                                for (Map.Entry<BlockPos, IBlockState> entry : connectionData.oldBlockStates.entrySet()) world.setBlockState(entry.getKey(), entry.getValue());
                                player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Cancelled pending connection and undid block changes"));
                            }
                            else player.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot cancel; no connection is currently in progress"));
                        }
                        else if (args[1].equals("mode"))
                        {
                            if (args.length < 3) sender.sendMessage(new TextComponentString(getUsage(sender)));
                            else
                            {
                                if (args[2].equals("twoWay") || args[2].equals("exitOnly") || args[2].equals("entranceOnly"))
                                {
                                    BlockPos[] blocksInRay = ImprovedRayTracing.blocksInRay(player, 200, true);
                                    BlockPos pos = blocksInRay[blocksInRay.length - 1];
                                    File file = new File(MCTools.getWorldSaveDir(server) + MCTools.getSaveFolder(player.world.provider) + File.separator + "Connect " + EnumFacing.getDirectionFromEntityLiving(pos, player) + " " + pos + ".txt");
                                    if (file.exists())
                                    {
                                        try
                                        {
                                            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                                            writer.write(args[2] + "\r\n");
                                            writer.close();
                                        }
                                        catch (IOException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                    else sender.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot change mode; no connection exists at " + pos));
                                }
                                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                            }
                        }
                        else sender.sendMessage(new TextComponentString(getUsage(sender)));
                    }
                }
                break;


            case "roomTags":
                if (!(sender instanceof EntityPlayerMP)) sender.sendMessage(new TextComponentString(TextFormatting.RED + "This command is only usable by players"));
                else if (args.length < 3 || !(args[1].equals("add") || args[1].equals("remove"))) sender.sendMessage(new TextComponentString(getUsage(sender)));
                else
                {
                    player = (EntityPlayerMP) sender;
                    DimensionType type = player.world.provider.getDimensionType();
                    if (type != InstanceTypes.ROOM_TILE) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Room tag command is only available in room tiles"));
                    else
                    {
                        File file = new File(MCTools.getWorldSaveDir(server) + MCTools.getSaveFolder(player.world.provider) + File.separator + "Tags.txt");
                        try
                        {
                            HashSet<String> tags = new HashSet<>();
                            if (file.exists())
                            {
                                BufferedReader reader = new BufferedReader(new FileReader(file));
                                String line = reader.readLine();
                                while (line != null)
                                {
                                    tags.add(line);
                                    line = reader.readLine();
                                }
                            }

                            if (args[1].equals("add")) tags.add(args[2]);
                            else tags.remove(args[2]);

                            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                            for (String tag : tags) writer.write(tag + "\r\n");
                            writer.close();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                break;


            case "roomTile":
                if (sender instanceof EntityPlayerMP && args.length > 1)
                {
                    data = InstanceData.get(true, InstanceTypes.ROOM_TILE, args[1]);
                    if (data != null) Teleport.joinPossiblyCreating((EntityPlayerMP) sender, data.getFullName());
                    else sender.sendMessage(new TextComponentString(getUsage(sender)));
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;


            case "joinTempCopy":
                server.profiler.startSection("joinTempCopy");

                if (args.length == 2)
                {
                    if (sender instanceof EntityPlayerMP)
                    {
                        player = (EntityPlayerMP) sender;
                    }
                }

                if (args.length < 3 && player == null) sender.sendMessage(new TextComponentString(getUsage(sender)));
                else
                {
                    data = InstanceData.get(args[1]);
                    if (data == null || !data.exists())
                    {
                        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find instance: " + args[1]));

                        server.profiler.endSection();
                        break;
                    }

                    if (player == null)
                    {
                        player = (EntityPlayerMP) PlayerData.getEntity(args[2]);
                        if (player == null)
                        {
                            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find player: " + args[2]));

                            server.profiler.endSection();
                            break;
                        }
                    }

                    if (args.length < 6) Teleport.joinTempCopy(player, data.getFullName());
                    else
                    {
                        Vec3d position = new Vec3d(Double.parseDouble(args[3].trim()), Double.parseDouble(args[4].trim()), Double.parseDouble(args[5].trim()));
                        Teleport.joinTempCopy(player, data.getFullName(), position);
                    }
                }

                server.profiler.endSection();
                break;


            case "join":
                if (args.length == 1)
                {
                    if (sender instanceof EntityPlayerMP)
                    {
                        player = (EntityPlayerMP) sender;
                        Teleport.teleport(player, player.dimension);
                    }
                }
                else
                {
                    data = InstanceData.get(args[1]);
                    if (data == null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find or create instance: " + args[1]));
                    else
                    {
                        if (args.length > 2)
                        {
                            player = (EntityPlayerMP) PlayerData.getEntity(args[2]);
                            if (player == null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find player: " + args[2]));
                            else
                            {
                                if (args.length < 6) Teleport.joinPossiblyCreating(player, data.getFullName());
                                else
                                {
                                    Vec3d position = new Vec3d(Double.parseDouble(args[3].trim()), Double.parseDouble(args[4].trim()), Double.parseDouble(args[5].trim()));
                                    Teleport.joinPossiblyCreating(player, data.getFullName(), position);
                                }
                            }
                        }
                        else
                        {
                            if (sender instanceof EntityPlayerMP)
                            {
                                Teleport.joinPossiblyCreating((EntityPlayerMP) sender, data.getFullName());
                            }
                            else sender.sendMessage(new TextComponentString(TextFormatting.RED + "No player name specified!"));
                        }
                    }
                }
                break;


            case "library":
                if (sender instanceof EntityPlayerMP)
                {
                    player = (EntityPlayerMP) sender;

                    data = InstanceData.get(false, InstanceTypes.LIBRARY_OF_WORLDS, player.getName() + "'s_Library_of_Worlds");
                    Teleport.joinPossiblyCreating(player, data.getFullName());
                }
                break;


            case "skyroom":
                if (sender instanceof EntityPlayerMP)
                {
                    player = (EntityPlayerMP) sender;
                    if (args.length == 1)
                    {
                        data = InstanceData.get(true, InstanceTypes.SKYROOM, "" + player.getPersistentID());
                    }
                    else
                    {
                        PlayerData otherPlayerData = PlayerData.get(args[1]);
                        if (otherPlayerData == null)
                        {
                            sender.sendMessage(new TextComponentString("Player " + args[1] + " not found"));
                            break;
                        }

                        data = InstanceData.get(true, InstanceTypes.SKYROOM, "" + otherPlayerData.id);
                    }

                    Teleport.joinPossiblyCreating(player, data.getFullName());
                }
                break;


            case "template":
                if (sender instanceof EntityPlayerMP && args.length > 1)
                {
                    data = InstanceData.get(true, InstanceTypes.TEMPLATE, args[1]);
                    if (data != null) Teleport.joinPossiblyCreating((EntityPlayerMP) sender, data.getFullName());
                    else sender.sendMessage(new TextComponentString(getUsage(sender)));
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;


            case "list":
                for (String s : InstanceHandler.instanceFolderNames(true))
                {
                    sender.sendMessage(new TextComponentString(s));
                }
                break;


            case "delete":
                if (args.length == 1)
                {
                    //Delete from within instance
                    if (sender instanceof EntityPlayerMP)
                    {
                        player = (EntityPlayerMP) sender;
                        data = InstanceData.get(MCTools.getSaveFolder(player.world.provider).replace("Instances" + File.separator, ""));
                        if (data == null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Deleting the 'current instance' only works while inside an instance"));
                        else InstanceHandler.delete(sender, data.getFullName());
                    }
                    else System.err.println("Deleting the 'current instance' is only usable when logged in as a player (and inside an instance)");
                }
                else
                {
                    if (args[1].contains(".."))
                    {
                        System.err.println(TextFormatting.RED + "WARNING! " + sender.getName() + " attempted to delete file with upwards directory access!  Check and make sure this is not a hostile action against the server!");
                        System.err.println(TextFormatting.RED + "Prevented deletion: " + new File(InstanceHandler.getInstancesDir(server) + args[1]).getAbsolutePath());
                        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Upwards directory accessors (..) are not allowed"));
                        break;
                    }

                    data = InstanceData.get(args[1]);
                    if (data == null || !data.exists()) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find instance to delete: " + args[1]));
                    else InstanceHandler.delete(sender, data.getFullName());
                }
                break;


            case "copy":
                if (args.length < 3)
                {
                    sender.sendMessage(new TextComponentString(getUsage(sender)));
                    break;
                }

                String oldName = Tools.fixFileSeparators(args[1]), newName = Tools.fixFileSeparators(args[2]);
                if (oldName.contains("..") || newName.contains(".."))
                {
                    System.err.println(TextFormatting.RED + "WARNING! " + sender.getName() + " attempted to copy to and/or from file with upwards directory access!  Check and make sure this is not a hostile action against the server!");
                    System.err.println(TextFormatting.RED + "Prevented copy: " + new File(InstanceHandler.getInstancesDir(server) + oldName).getAbsolutePath() + " -> " + new File(InstanceHandler.getInstancesDir(server) + newName).getAbsolutePath());
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Upwards directory accessors (..) are not allowed"));
                    break;
                }

                InstanceData oldData = InstanceData.get(oldName), newData = InstanceData.get(newName);
                if (oldData == null || newData == null || !oldData.exists()) break;

                InstanceHandler.copyInstance(sender, oldName, newName);

                if (args.length > 3) Owners.setOwner(newData.getFullName(), PlayerData.getID(args[3]));
                else Owners.setOwner(newData.getFullName(), oldData.getOwner());
                break;


            case "setOwner":
                if (args.length < 3) sender.sendMessage(new TextComponentString(getUsage(sender)));
                else
                {
                    data = InstanceData.get(args[1]);
                    if (data == null) sender.sendMessage(new TextComponentString(getUsage(sender)));
                    else Owners.setOwner(data.getFullName(), PlayerData.getID(args[2]));
                }
                break;


            case "removeOwner":
                if (args.length < 2) sender.sendMessage(new TextComponentString(getUsage(sender)));
                else
                {
                    data = InstanceData.get(args[1]);
                    if (data == null) sender.sendMessage(new TextComponentString(getUsage(sender)));
                    else Owners.setOwner(data.getFullName(), null);
                }
                break;


            default:
                sender.sendMessage(new TextComponentString(getUsage(sender)));
        }
    }


    @SubscribeEvent
    public static void entityJoinWorld(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();
        if (entity instanceof EntityPlayerMP)
        {
            if (pendingConnectionData.containsKey(event.getWorld()))
            {
                ServerTickTimer.schedule(1, () ->
                {
                    Teleport.escape(entity);
                    entity.sendMessage(new TextComponentString(TextFormatting.RED + "Could not join room tile instance; someone is currently editing a room connection"));
                });
            }
        }
    }

    @SubscribeEvent
    public static void onBlockChange(WorldEventDistributor.DBlockUpdateEvent event)
    {
        if (!MCTools.hosting()) return;

        ConnectionData data = pendingConnectionData.get(event.getWorld());
        if (data != null && !data.oldBlockStates.containsKey(event.pos)) data.oldBlockStates.put(event.pos, event.oldState);
    }

    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event)
    {
        World world = event.world;
        ConnectionData data = pendingConnectionData.get(world);
        if (data != null && !world.playerEntities.contains(data.editingPlayer)) pendingConnectionData.remove(world);
    }


    public static class ConnectionData
    {
        EntityPlayerMP editingPlayer;
        InstanceData instanceData;
        BlockPos pos;
        EnumFacing facing;
        HashMap<BlockPos, IBlockState> oldBlockStates = new HashMap<>();

        public ConnectionData(EntityPlayerMP player)
        {
            editingPlayer = player;
            instanceData = InstanceData.get(player);
            BlockPos[] blocksInRay = ImprovedRayTracing.blocksInRay(player, 200, true);
            pos = blocksInRay[blocksInRay.length - 1];
            facing = EnumFacing.getDirectionFromEntityLiving(pos, player);
        }
    }
}
