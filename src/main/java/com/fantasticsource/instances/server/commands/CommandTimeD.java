package com.fantasticsource.instances.server.commands;

import com.fantasticsource.instances.Instances;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.List;

public class CommandTimeD extends CommandBase
{

    @Override
    public String getName()
    {
        return "timed";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return Instances.MODID + ".commands.timed.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 2)
        {
            int dimension = Integer.parseInt(args[0]);
            int i;

            WorldServer worldserver = DimensionManager.getWorld(dimension);

            if (worldserver == null)
            {
                notifyCommandListener(sender, this, "No dimension found with the id %s", dimension);
                return;
            }

            if (args[1].equals("set"))
            {
                switch (args[2])
                {
                    case "day":
                        i = 1000;
                        break;

                    case "night":
                        i = 13000;
                        break;

                    default:
                        i = parseInt(args[2], 0);
                        break;
                }

                worldserver.setWorldTime(i);
                notifyCommandListener(sender, this, "commands.time.set", i);
                return;
            }

            if (args[1].equals("add"))
            {
                i = parseInt(args[2], 0);
                worldserver.setWorldTime(worldserver.getWorldTime() + i);
                notifyCommandListener(sender, this, "commands.time.added", i);
                return;
            }

            if (args[1].equals("query"))
            {
                if (args[2].equals("daytime"))
                {
                    i = (int) (sender.getEntityWorld().getWorldTime() % 2147483647L);
                    sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, i);
                    notifyCommandListener(sender, this, "commands.time.query", i);
                    return;
                }

                if (args[2].equals("gametime"))
                {
                    i = (int) (sender.getEntityWorld().getTotalWorldTime() % 2147483647L);
                    sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, i);
                    notifyCommandListener(sender, this, "commands.time.query", i);
                    return;
                }
            }
        }

        throw new WrongUsageException(Instances.MODID + ".commands.timed.usage");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 2 ? getListOfStringsMatchingLastWord(args, "set", "add", "query") : (args.length == 3 && args[1].equals("set") ? getListOfStringsMatchingLastWord(args, "day", "night") : (args.length == 3 && args[1].equals("query") ? getListOfStringsMatchingLastWord(args, "daytime", "gametime") : null));
    }
}
