package com.fantasticsource.instances.server.commands;

import com.fantasticsource.instances.network.PacketHandler;
import com.fantasticsource.instances.network.messages.MessageOpenGui;
import com.fantasticsource.instances.server.InstanceHandler;
import com.fantasticsource.instances.util.WorldInfoSimple;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.PlayerData;
import com.fantasticsource.tools.datastructures.Pair;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Commands extends CommandBase
{

    private static ArrayList<String> playernames()
    {
        ArrayList<String> strings = new ArrayList<>();

        for (PlayerData data : PlayerData.playerData.values())
        {
            strings.add(data.name);
        }

        return strings;
    }

    public static boolean joinPossiblyCreating(EntityPlayerMP owner, MinecraftServer server)
    {
        return joinPossiblyCreating(owner, owner.getName(), server);
    }

    public static boolean joinPossiblyCreating(Entity entity, String ownername, MinecraftServer server)
    {
        UUID id = PlayerData.getID(ownername);
        if (id == null) return false;

        //Try finding any instance owned by the player
        for (Map.Entry<Integer, WorldInfoSimple> entry : InstanceHandler.instanceInfo.entrySet())
        {
            if (entry.getValue().getOwner().equals(id))
            {
                return CommandTeleportD.tpd(entity, entry.getKey(), 0, 76, 0, 0, 0);
            }
        }

        //Not found
        Pair<Integer, WorldInfoSimple> pair = InstanceHandler.createDimension(entity, InstanceTypes.skyroomDimType, id, ownername + "'s " + InstanceTypes.skyroomDimType.name());
        return CommandTeleportD.tpd(entity, pair.getKey(), 0, 76, 0, 0, 0);
    }

    @Override
    public String getName()
    {
        return "instances";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "Usage: /instances <create:delete:list:setowner:personal>";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            sender.sendMessage(new TextComponentString(getUsage(sender)));
            return;
        }

        switch (args[0])
        {
            case "personal":
                if (sender instanceof EntityPlayerMP)
                {
                    if (args.length == 1)
                    {
                        if (!joinPossiblyCreating((EntityPlayerMP) sender, server))
                        {
                            sender.sendMessage(new TextComponentString("Player " + args[1] + " not found"));
                        }
                    }
                    else if (args.length == 2)
                    {
                        if (!joinPossiblyCreating((EntityPlayerMP) sender, args[1], server))
                        {
                            sender.sendMessage(new TextComponentString("Player " + args[1] + " not found"));
                        }
                    }
                    else sender.sendMessage(new TextComponentString(getUsage(sender)));
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;

            case "list":
                for (String s : InstanceHandler.list())
                {
                    sender.sendMessage(new TextComponentString(s));
                }
                break;

            case "create":
                if (args.length == 1)
                {
                    //GUI
                    if (sender instanceof EntityPlayerMP && !(sender instanceof FakePlayer))
                    {
                        EntityPlayerMP player = (EntityPlayerMP) sender;

                        PacketHandler.INSTANCE.sendTo(new MessageOpenGui(), player);
                    }
                }
                else if (args.length == 3)
                {
                    //No GUI
                    InstanceHandler.createDimension(sender, DimensionType.byName(args[1]), PlayerData.getID(args[2]), args[2] + "'s " + args[1]);
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;

            case "delete":
                if (args.length == 2)
                {
                    int dimensionID = Integer.parseInt(args[1]);

                    InstanceHandler.deleteDimension(sender, dimensionID);
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;

            case "setowner":
                if (args.length == 3)
                {
                    int dimID;
                    try
                    {
                        dimID = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException e)
                    {
                        sender.sendMessage(new TextComponentString(args[1] + " is not a (instance ID) number"));
                        return;
                    }

                    WorldInfoSimple info = InstanceHandler.get(dimID);
                    if (info == null)
                    {
                        sender.sendMessage(new TextComponentString("Instance ID (" + args[1] + ") not found"));
                        return;
                    }

                    UUID id = PlayerData.getID(args[2]);
                    if (id == null)
                    {
                        sender.sendMessage(new TextComponentString("Player (" + args[2] + ") not found"));
                        return;
                    }

                    info.setOwner(id);
                    sender.sendMessage(new TextComponentString("Set owner of " + info.getWorldName() + " to " + PlayerData.getName(id) + " (ID = " + args[1] + ", type = " + info.getDimensionType().name() + ")"));
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;

            default:
                sender.sendMessage(new TextComponentString(getUsage(sender)));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "create", "delete", "list", "setowner", "personal");
        }
        else if (args.length == 2)
        {
            if (args[0].equals("delete") || args[0].equals("setowner"))
            {
                ArrayList<String> strings = new ArrayList<>();

                for (int i : InstanceHandler.instanceInfo.keySet()) strings.add("" + i);

                return getListOfStringsMatchingLastWord(args, strings);
            }
            else if (args[0].equals("create"))
            {
                ArrayList<String> strings = new ArrayList<>();

                for (DimensionType type : DimensionType.values())
                {
                    strings.add(type.getName());
                }

                return getListOfStringsMatchingLastWord(args, strings);
            }
            else if (args[0].equals("personal"))
            {
                return getListOfStringsMatchingLastWord(args, playernames());
            }
            else return new ArrayList<>();
        }
        else if (args.length == 3)
        {
            if (args[0].equals("setowner") || args[0].equals("create"))
            {
                return getListOfStringsMatchingLastWord(args, playernames());
            }
            else return new ArrayList<>();
        }
        else
        {
            return new ArrayList<>();
        }
    }
}
