package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.instances.world.dimensions.libraryofworlds.VisitablePlayersData;
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

        InstanceWorldInfo info = null;
        for (InstanceWorldInfo info1 : InstanceHandler.instanceInfo.values())
        {
            if (player.getPersistentID().equals(info1.getOwner()))
            {
                info = info1;
                break;
            }
        }

        if (args.length == 0)
        {
            player.sendMessage(new TextComponentString("Allowed Visitors:"));
            if (info != null)
            {
                for (UUID id : info.visitorWhitelist) player.sendMessage(new TextComponentString(PlayerData.getName(id)));
            }
            return;
        }

        PlayerData playerData = PlayerData.get(args[0]);
        if (playerData == null)
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
            if (info == null || !info.visitorWhitelist.contains(playerData.id)) player.sendMessage(new TextComponentString(args[0] + " is currently NOT allowed to visit you"));
            else player.sendMessage(new TextComponentString(args[0] + " is currently allowed to visit you"));
        }
        else if (args.length == 2)
        {
            if (args[1].toLowerCase().equals("allow"))
            {
                if (info == null)
                {
                    info = InstanceHandler.createInstance(null, InstanceTypes.skyroomDimType, player.getPersistentID(), false).getValue();
                }

                if (info.visitorWhitelist.contains(playerData.id))
                {
                    player.sendMessage(new TextComponentString(args[0] + " can visit you"));
                    return;
                }

                InstanceHandler.visitablePlayersData.computeIfAbsent(playerData.id, o -> new VisitablePlayersData()).add(player.getPersistentID());


                info.visitorWhitelist.add(playerData.id);
                InstanceHandler.trySave(info);


                player.sendMessage(new TextComponentString(args[0] + " can now visit you"));
            }
            else if (args[1].toLowerCase().equals("deny"))
            {
                if (info == null || !info.visitorWhitelist.contains(playerData.id))
                {
                    player.sendMessage(new TextComponentString(args[0] + " cannot visit you"));
                    return;
                }

                EntityPlayerMP otherPlayer = (EntityPlayerMP) playerData.player;
                if (otherPlayer != null)
                {
                    InstanceWorldInfo info1 = InstanceHandler.get(otherPlayer.dimension);
                    if (info1 != null && player.getPersistentID().equals(info1.getOwner())) Teleport.escape(otherPlayer);
                }

                VisitablePlayersData visitData = InstanceHandler.visitablePlayersData.get(playerData.id);
                if (visitData != null)
                {
                    visitData.remove(player.getPersistentID());
                    if (visitData.visitablePlayers.size() == 0) InstanceHandler.visitablePlayersData.remove(playerData.id);
                }


                info.visitorWhitelist.remove(playerData.id);
                InstanceHandler.trySave(info);


                player.sendMessage(new TextComponentString(args[0] + " can no longer visit you"));
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
