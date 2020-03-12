package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.PlayerData;
import com.fantasticsource.tools.Tools;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;

import java.io.File;
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
        return "Usage: /instances <hub:personal:template:list:delete:copy>";
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
                if (sender instanceof EntityPlayerMP)
                {
                    EntityPlayerMP player = (EntityPlayerMP) sender;
                    Teleport.joinPossiblyCreating(player, InstanceTypes.libraryOfWorldsDimType, "" + player.getPersistentID(), player.getPersistentID());
                }
                break;

            case "personal":
                if (sender instanceof EntityPlayerMP)
                {
                    if (args.length == 1)
                    {
                        EntityPlayerMP player = (EntityPlayerMP) sender;
                        Teleport.joinPossiblyCreating(player, InstanceTypes.skyroomDimType, "" + player.getPersistentID(), player.getPersistentID());
                    }
                    else if (args.length == 2)
                    {
                        EntityPlayerMP player = (EntityPlayerMP) sender;
                        PlayerData otherPlayerData = PlayerData.get(args[1]);
                        if (otherPlayerData == null) sender.sendMessage(new TextComponentString("Player " + args[1] + " not found"));
                        else Teleport.joinPossiblyCreating(player, InstanceTypes.skyroomDimType, "" + otherPlayerData.id, otherPlayerData.id);
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

                    if (!name.toString().equals("")) Teleport.joinPossiblyCreating((EntityPlayerMP) sender, InstanceTypes.templateDimType, name.toString(), null);
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
                        if (args[1].contains(".."))
                        {
                            System.err.println(TextFormatting.RED + "WARNING! " + sender.getName() + " attempted to delete file with upwards directory access!  Check and make sure this is not a hostile action against the server!");
                            System.err.println(TextFormatting.RED + "Prevented deletion: " + new File(InstanceHandler.getInstancesDir(server) + args[1]).getAbsolutePath());
                            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Upwards directory accessors (..) are not allowed"));
                            break;
                        }

                        //Delete by instance folder
                        boolean done = false;
                        for (WorldServer world : server.worlds)
                        {
                            if (world.getWorldInfo() instanceof InstanceWorldInfo && world.provider.getSaveFolder().equals(args[1]))
                            {
                                InstanceHandler.delete(sender, world);
                                done = true;
                                break;
                            }
                        }

                        //Delete by folder name
                        if (!done) InstanceHandler.delete(sender, args[1]);
                    }
                }
                else sender.sendMessage(new TextComponentString(getUsage(sender)));
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

                //Explicit instance type selection
                DimensionType instanceType = null;
                if (args.length > 3)
                {
                    for (DimensionType t : InstanceTypes.instanceTypes)
                    {
                        if (t.getName().equals(args[3]))
                        {
                            instanceType = t;
                            break;
                        }
                    }
                }

                //Instance type based on new world name
                if (instanceType == null)
                {
                    String newTypeName = newName.substring(0, newName.indexOf(File.separator));
                    for (DimensionType t : InstanceTypes.instanceTypes)
                    {
                        if (t.getName().equals(newTypeName))
                        {
                            instanceType = t;
                            break;
                        }
                    }
                }

                //Instance type based on old world name
                if (instanceType == null)
                {
                    String oldTypeName = oldName.substring(0, oldName.indexOf(File.separator));
                    for (DimensionType t : InstanceTypes.instanceTypes)
                    {
                        if (t.getName().equals(oldTypeName))
                        {
                            instanceType = t;
                            break;
                        }
                    }
                }

                //Default instance type
                if (instanceType == null) instanceType = InstanceTypes.templateDimType;

                boolean save = args.length <= 4 || Boolean.parseBoolean(args[4]);
                UUID owner = args.length > 5 ? PlayerData.getID(args[5]) : null;

                InstanceHandler.copyInstance(sender, oldName, newName, instanceType, owner, save);

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
            return getListOfStringsMatchingLastWord(args, "hub", "personal", "template", "list", "delete", "copy");
        }
        else if (args.length == 2)
        {
            if (args[0].equals("delete") || args[0].equals("copy"))
            {
                return getListOfStringsMatchingLastWord(args, InstanceHandler.instanceFolderNames(true));
            }
            else if (args[0].equals("personal"))
            {
                return getListOfStringsMatchingLastWord(args, playernames());
            }
            else if (args[0].equals("template"))
            {
                return getListOfStringsMatchingLastWord(args, InstanceHandler.instanceFolderNames(InstanceTypes.templateDimType, false));
            }
        }
        else if (args.length == 4)
        {
            if (args[0].equals("copy"))
            {
                return getListOfStringsMatchingLastWord(args, "true", "false");
            }
        }
        else if (args.length == 5)
        {
            if (args[0].equals("copy"))
            {
                return getListOfStringsMatchingLastWord(args, playernames());
            }
        }

        return new ArrayList<>();
    }
}
