package com.fantasticsource.instances.server.commands;

import com.fantasticsource.instances.network.PacketHandler;
import com.fantasticsource.instances.network.messages.MessageOpenGui;
import com.fantasticsource.instances.server.InstanceHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.List;

public class Commands extends CommandBase
{

    @Override
    public String getName()
    {
        return "instances";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "Usage: /instances <create:delete:list>";
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
            case "create":
                if (sender instanceof EntityPlayerMP && !(sender instanceof FakePlayer))
                {
                    EntityPlayerMP player = (EntityPlayerMP) sender;

                    PacketHandler.INSTANCE.sendTo(new MessageOpenGui(), player);
                }
                break;
            case "delete":
                if (args.length == 1)
                {
                    sender.sendMessage(new TextComponentString("Usage: /instances delete <id>"));
                }
                if (args.length == 2)
                {
                    int dimensionID = Integer.parseInt(args[1]);

                    InstanceHandler.deleteDimension(sender, dimensionID);
                }
                break;
            case "list":
                break;
            default:
                sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "create", "delete", "list");
        }
        else
        {
            return new ArrayList<>();
        }
    }
}
