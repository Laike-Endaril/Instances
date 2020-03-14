package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.InstanceData;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.tags.savefile.Owners;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.PlayerData;
import com.fantasticsource.tools.Tools;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//TODO Come through here and do proper errors and usages for everything sometime...maybe
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
        return "Usage: /instances <library:skyroom:template:list:delete:copy:join:setOwner:joinTempCopy>";
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

        EntityPlayerMP player = null;
        InstanceData data = null;
        switch (args[0])
        {
            case "joinTempCopy":
                if (args.length < 3) sender.sendMessage(new TextComponentString(getUsage(sender)));
                else
                {
                    data = InstanceData.get(args[1]);
                    if (data == null || !data.exists())
                    {
                        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find instance: " + args[1]));
                        break;
                    }

                    player = (EntityPlayerMP) PlayerData.getEntity(args[2]);
                    if (player == null)
                    {
                        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find player: " + args[2]));
                        break;
                    }

                    Teleport.joinTempCopy(player, data.getFullName());
                }
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
                            else Teleport.joinPossiblyCreating(player, data.getFullName());
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
                if (args.length > 3)
                {
                    UUID playerID = PlayerData.getID(args[3]);
                    Owners.setOwner(FMLCommonHandler.instance().getMinecraftServerInstance(), newData.getFullName(), playerID != null ? "" + playerID : args[3]);
                }

                break;

            case "setOwner":
                if (args.length < 3) sender.sendMessage(new TextComponentString(getUsage(sender)));
                else
                {
                    data = InstanceData.get(args[1]);
                    if (data == null) sender.sendMessage(new TextComponentString(getUsage(sender)));
                    else
                    {
                        UUID playerID = PlayerData.getID(args[2]);
                        Owners.setOwner(FMLCommonHandler.instance().getMinecraftServerInstance(), data.getFullName(), playerID != null ? "" + playerID : args[2]);
                    }
                }
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
            return getListOfStringsMatchingLastWord(args, "library", "skyroom", "template", "list", "delete", "copy", "join", "setOwner", "joinTempCopy");
        }
        else if (args.length == 2)
        {
            if (args[0].equals("delete") || args[0].equals("copy") || args[0].equals("join") || args[0].equals("setOwner") || args[0].equals("joinTempCopy"))
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
        }
        else if (args.length == 3)
        {
            if (args[0].equals("join") || args[0].equals("setOwner") || args[0].equals("joinTempCopy"))
            {
                return getListOfStringsMatchingLastWord(args, playernames());
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
}
