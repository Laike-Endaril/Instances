package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.tags.savefile.Visitors;
import com.fantasticsource.mctools.PlayerData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CmdVisitors extends CommandBase
{
    @Override
    public String getName()
    {
        return "visitors";
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
        return "visitors <playername> [allow:deny]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (!(sender instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (args.length == 0)
        {
            player.sendMessage(new TextComponentString("Allowed Visitors:"));
            for (String id : Visitors.visitables(server, player.getPersistentID())) player.sendMessage(new TextComponentString(PlayerData.getName(UUID.fromString(id))));
            return;
        }

        PlayerData otherPlayerData = PlayerData.get(args[0]);
        if (otherPlayerData == null)
        {
            player.sendMessage(new TextComponentString("Player not found: " + args[0]));
            return;
        }

        if (args[0].equals(player.getName()))
        {
            player.sendMessage(new TextComponentString("Yes...you can visit yourself anytime you like"));
            return;
        }

        if (args.length == 1)
        {
            if (!Visitors.canBeVisitedBy(server, player.getPersistentID(), otherPlayerData.id)) player.sendMessage(new TextComponentString(args[0] + " is currently NOT allowed to visit you"));
            else player.sendMessage(new TextComponentString(args[0] + " is currently allowed to visit you"));
        }
        else if (args.length == 2)
        {
            if (args[1].toLowerCase().equals("allow"))
            {
                if (Visitors.setVisitable(server, otherPlayerData.id, player.getPersistentID(), true))
                {
                    player.sendMessage(new TextComponentString(args[0] + " can now visit you"));
                }
                else
                {
                    player.sendMessage(new TextComponentString(args[0] + " can already visit you"));
                }
            }
            else if (args[1].toLowerCase().equals("deny"))
            {
                if (Visitors.setVisitable(server, otherPlayerData.id, player.getPersistentID(), false))
                {
                    player.sendMessage(new TextComponentString(args[0] + " can no longer visit you"));
                }
                else
                {
                    player.sendMessage(new TextComponentString(args[0] + " already cannot visit you"));
                }
            }
            else player.sendMessage(new TextComponentString(getUsage(player)));
        }
        else player.sendMessage(new TextComponentString(getUsage(player)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, Commands.playernames());
        else if (args.length == 2) return getListOfStringsMatchingLastWord(args, Arrays.asList("allow", "deny"));
        return new ArrayList<>();
    }
}
