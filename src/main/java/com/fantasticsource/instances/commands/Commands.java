package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.mctools.PlayerData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Commands extends CommandBase
{
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
        return "Usage: /instances <hub:personal:template:list:delete:setowner>";
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
            case "hub":
                if (sender instanceof EntityPlayerMP) Teleport.gotoHub((EntityPlayerMP) sender);
                break;

            case "personal":
                if (sender instanceof EntityPlayerMP)
                {
                    if (args.length == 1)
                    {
                        Teleport.joinSkyroomPossiblyCreating((EntityPlayerMP) sender);
                    }
                    else if (args.length == 2)
                    {
                        if (!Teleport.joinSkyroomPossiblyCreating((EntityPlayerMP) sender, args[1]))
                        {
                            sender.sendMessage(new TextComponentString("Player " + args[1] + " not found"));
                        }
                    }
                    else sender.sendMessage(new TextComponentString(getUsage(sender)));
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;

            case "template":
                if (sender instanceof EntityPlayerMP)
                {
                    if (args.length == 1)
                    {
                        Teleport.joinSkyroomPossiblyCreating((EntityPlayerMP) sender);
                    }
                    else if (args.length == 2)
                    {
                        if (!Teleport.joinSkyroomPossiblyCreating((EntityPlayerMP) sender, args[1]))
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

            case "delete":
                if (args.length <= 2)
                {
                    InstanceWorldInfo info = null;
                    if (args.length == 1)
                    {
                        if (sender instanceof EntityPlayerMP)
                        {
                            info = InstanceHandler.get(((EntityPlayerMP) sender).dimension);
                            if (info == null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Deleting the 'current instance' only works while inside an instance"));
                        }
                        else System.err.println("Deleting the 'current instance' is only usable when logged in as a player (and inside an instance)");
                    }
                    else
                    {
                        try
                        {
                            info = InstanceHandler.get(Integer.parseInt(args[1]));
                        }
                        catch (NumberFormatException e)
                        {
                            for (InstanceWorldInfo info1 : InstanceHandler.instanceInfo.values())
                            {
                                if (info1.getWorldName().equals(args[1]))
                                {
                                    info = info1;
                                    break;
                                }
                            }
                        }
                    }

                    if (info != null) InstanceHandler.delete(sender, info);
                    else sender.sendMessage(new TextComponentString(getUsage(sender)));
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;

            case "setowner":
                if (args.length == 3)
                {
                    InstanceWorldInfo info = null;
                    try
                    {
                        info = InstanceHandler.get(Integer.parseInt(args[1]));
                    }
                    catch (NumberFormatException e)
                    {
                        for (InstanceWorldInfo info1 : InstanceHandler.instanceInfo.values())
                        {
                            if (info1.getWorldName().equals(args[1]))
                            {
                                info = info1;
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
                    InstanceHandler.trySave(info);

                    sender.sendMessage(new TextComponentString("Set owner of " + info.getWorldName() + " to " + PlayerData.getName(id) + " (ID = " + args[1] + ", type = " + info.getDimensionType().getName() + ")"));
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
            return getListOfStringsMatchingLastWord(args, "delete", "list", "setowner", "personal", "hub");
        }
        else if (args.length == 2)
        {
            if (args[0].equals("delete") || args[0].equals("setowner"))
            {
                ArrayList<String> strings = new ArrayList<>();

                for (InstanceWorldInfo info : InstanceHandler.instanceInfo.values()) strings.add(info.getWorldName());
                for (int i : InstanceHandler.instanceInfo.keySet()) strings.add("" + i);

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
            if (args[0].equals("setowner")) return getListOfStringsMatchingLastWord(args, playernames());
            else return new ArrayList<>();
        }
        else
        {
            return new ArrayList<>();
        }
    }
}
