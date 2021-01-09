package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.PlayerData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CmdEscape extends CommandBase
{
    @Override
    public String getName()
    {
        return "escapeinstance";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "escapeinstance [playername]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            if (sender instanceof EntityPlayerMP)
            {
                EntityPlayerMP player = (EntityPlayerMP) sender;
                if (player.isCreative())
                {
                    Teleport.escape(player);
                    return;
                }

                DimensionType type = player.world.provider.getDimensionType();
                if (type == InstanceTypes.SKYROOM || type == InstanceTypes.LIBRARY_OF_WORLDS)
                {
                    Teleport.escape(player);
                    return;
                }

                sender.sendMessage(new TextComponentString(TextFormatting.RED + "This command can only be used when in a skyroom or Library of Worlds instance"));
            }
            else sender.sendMessage(new TextComponentString(getUsage(sender)));
        }
        else
        {
            if (sender instanceof EntityPlayerMP && !MCTools.isOP((EntityPlayerMP) sender))
            {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Only OPs can use this on specific players"));
            }
            else
            {
                EntityPlayerMP player = (EntityPlayerMP) PlayerData.getEntity(args[0]);
                if (player == null) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find player: " + args[0]));
                else
                {
                    Teleport.escape(player);
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, playernames());

        return new ArrayList<>();
    }

    public static ArrayList<String> playernames()
    {
        ArrayList<String> strings = new ArrayList<>();

        for (PlayerData data : PlayerData.playerData.values())
        {
            strings.add(data.name);
        }

        return strings;
    }
}
