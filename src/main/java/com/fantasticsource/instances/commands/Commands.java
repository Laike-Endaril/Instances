package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.PlayerData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;

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
        return "Usage: /instances <hub:personal:template:list:delete>";
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
                if (sender instanceof EntityPlayerMP) Teleport.joinHubPossiblyCreating((EntityPlayerMP) sender);
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
                        PlayerData otherPlayerData = PlayerData.get(args[1]);
                        if (otherPlayerData == null || !Teleport.joinSkyroomPossiblyCreating((EntityPlayerMP) sender, otherPlayerData.id))
                        {
                            sender.sendMessage(new TextComponentString("Player " + args[1] + " not found"));
                        }
                    }
                    else sender.sendMessage(new TextComponentString(getUsage(sender)));
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;

            case "template":
                if (sender instanceof EntityPlayerMP && args.length > 1)
                {
                    StringBuilder name = new StringBuilder(args[1]);
                    for (int i = 3; i < args.length; i++) name.append("_").append(args[i]);

                    if (!name.toString().equals("")) Teleport.joinTemplatePossiblyCreating((EntityPlayerMP) sender, name.toString());
                    else sender.sendMessage(new TextComponentString(getUsage(sender)));
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;

            case "list":
                for (String s : InstanceHandler.instanceFolderNames())
                {
                    sender.sendMessage(new TextComponentString(s));
                }
                break;

            case "delete":
                if (args.length <= 2)
                {
                    InstanceWorldInfo info = null; //This null is necessary!
                    if (args.length == 1)
                    {
                        //Delete from within instance
                        if (sender instanceof EntityPlayerMP)
                        {
                            info = InstanceHandler.get(((EntityPlayerMP) sender).dimension);
                            if (info == null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Deleting the 'current instance' only works while inside an instance"));
                            else InstanceHandler.delete(sender, info);
                        }
                        else System.err.println("Deleting the 'current instance' is only usable when logged in as a player (and inside an instance)");
                    }
                    else
                    {
                        //Delete by instance folder
                        boolean done = false;
                        for (WorldServer world : server.worlds)
                        {
                            if (world.getWorldInfo() instanceof InstanceWorldInfo && world.provider.getSaveFolder().equals(args[1]))
                            {
                                if (InstanceHandler.delete(sender, world))
                                {
                                    done = true;
                                    break;
                                }
                            }
                        }

                        if (!done)
                        {
                            //Delete by filename
                            if (!InstanceHandler.delete(sender, args[1]))
                            {
                                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find instance to delete: " + args[1]));
                            }
                        }
                    }
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
            return getListOfStringsMatchingLastWord(args, "hub", "personal", "template", "list", "delete");
        }
        else if (args.length == 2)
        {
            if (args[0].equals("delete"))
            {
                return getListOfStringsMatchingLastWord(args, InstanceHandler.instanceFolderNames());
            }
            else if (args[0].equals("personal"))
            {
                return getListOfStringsMatchingLastWord(args, playernames());
            }
            else if (args[0].equals("template"))
            {
                return getListOfStringsMatchingLastWord(args, InstanceHandler.instanceFolderNames(InstanceTypes.templateDimType, false));
            }
            else return new ArrayList<>();
        }
        else
        {
            return new ArrayList<>();
        }
    }
}
