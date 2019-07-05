package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.network.PacketHandler;
import com.fantasticsource.instances.network.messages.MessageOpenGui;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.WorldInfoSimple;
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

    public static boolean gotoHub(EntityPlayerMP player)
    {
        //Try finding an existing hub for said player
        for (Map.Entry<Integer, WorldInfoSimple> entry : InstanceHandler.instanceInfo.entrySet())
        {
            WorldInfoSimple info = entry.getValue();
            if (info.getDimensionType() == InstanceTypes.skyhubDimType && info.getWorldName().equals(player.getName() + "'s " + InstanceTypes.skyhubDimType.name()))
            {
                return CmdTPD.tpd(player, entry.getKey(), 0, 77, -13.5, player.rotationYaw, player.rotationPitch);
            }
        }

        //Not found
        Pair<Integer, WorldInfoSimple> pair = InstanceHandler.createDimension(player, InstanceTypes.skyhubDimType, null, player.getName() + "'s " + InstanceTypes.skyhubDimType.name());
        return CmdTPD.tpd(player, pair.getKey(), 0, 77, -13.5, player.rotationYaw, player.rotationPitch);
    }

    public static boolean joinPossiblyCreating(EntityPlayerMP player)
    {
        return joinPossiblyCreating(player, player.getName());
    }

    public static boolean joinPossiblyCreating(Entity entity, String ownername)
    {
        UUID id = PlayerData.getID(ownername);
        if (id == null) return false;

        //Try finding any instance owned by the player
        for (Map.Entry<Integer, WorldInfoSimple> entry : InstanceHandler.instanceInfo.entrySet())
        {
            if (entry.getValue().getOwner().equals(id))
            {
                return CmdTPD.tpd(entity, entry.getKey(), 0, 77, -13.5, entity.rotationYaw, entity.rotationPitch);
            }
        }

        //Not found
        Pair<Integer, WorldInfoSimple> pair = InstanceHandler.createDimension(entity, InstanceTypes.skyroomDimType, id, ownername + "'s " + InstanceTypes.skyroomDimType.name());
        return CmdTPD.tpd(entity, pair.getKey(), 0, 77, -13.5, entity.rotationYaw, entity.rotationPitch);
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
                        if (!joinPossiblyCreating((EntityPlayerMP) sender))
                        {
                            sender.sendMessage(new TextComponentString("Player " + args[1] + " not found"));
                        }
                    }
                    else if (args.length == 2)
                    {
                        if (!joinPossiblyCreating((EntityPlayerMP) sender, args[1]))
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
                else if (args.length == 2)
                {
                    //No GUI, no owner
                    InstanceHandler.createDimension(sender, DimensionType.byName(args[1]), null, args[1] + " Instance");
                }
                else if (args.length == 3)
                {
                    //No GUI, has owner
                    InstanceHandler.createDimension(sender, DimensionType.byName(args[1]), PlayerData.getID(args[2]), args[2] + "'s " + args[1]);
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;

            case "delete":
                if (args.length == 2)
                {
                    int dimensionID;
                    try
                    {
                        dimensionID = Integer.parseInt(args[1]);
                        InstanceHandler.deleteDimension(sender, dimensionID);
                    }
                    catch (NumberFormatException e)
                    {
                        for (Map.Entry<Integer, WorldInfoSimple> entry : InstanceHandler.instanceInfo.entrySet())
                        {
                            if (entry.getValue().getWorldName().equals(args[1]))
                            {
                                InstanceHandler.deleteDimension(sender, entry.getKey());
                                return;
                            }
                        }
                        sender.sendMessage(new TextComponentString(getUsage(sender)));
                    }

                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;

            case "setowner":
                if (args.length == 3)
                {
                    WorldInfoSimple info = null;
                    try
                    {
                        info = InstanceHandler.get(Integer.parseInt(args[1]));
                    }
                    catch (NumberFormatException e)
                    {
                        for (WorldInfoSimple info2 : InstanceHandler.instanceInfo.values())
                        {
                            if (info2.getWorldName().equals(args[1]))
                            {
                                info = info2;
                                break;
                            }
                        }
                    }

                    if (info == null)
                    {
                        sender.sendMessage(new TextComponentString("Instance not found: " + args[1]));
                        return;
                    }

                    UUID id = PlayerData.getID(args[2]);
                    if (id == null)
                    {
                        sender.sendMessage(new TextComponentString("Player not found: " + args[2]));
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

                for (WorldInfoSimple info : InstanceHandler.instanceInfo.values()) strings.add(info.getWorldName());
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
