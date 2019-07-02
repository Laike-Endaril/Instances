package com.fantasticsource.instances.server.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

import java.util.List;
import java.util.Random;

public class CommandWeatherD extends CommandBase
{
    /**
     * Get the name of the command
     */
    @Override
    public String getName()
    {
        return "weatherd";
    }

    /**
     * Return the required permission level for this command.
     */
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "instances.commands.weatherd.usage";
    }

    /**
     * Called when a CommandSender executes this command
     */
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length >= 1 && args.length <= 3)
        {
            int dimension = Integer.parseInt(args[0]);

            int i = 6000 + new Random().nextInt(12000);

            if (args.length >= 3)
            {
                i = parseInt(args[2], 1, 1000000) * 20;
            }

            WorldServer worldserver = DimensionManager.getWorld(dimension);

            if (worldserver != null)
            {
                WorldInfo worldinfo = worldserver.getWorldInfo();

                if ("clear".equalsIgnoreCase(args[1]))
                {
                    worldinfo.setCleanWeatherTime(i);
                    worldinfo.setRainTime(0);
                    worldinfo.setThunderTime(0);
                    worldinfo.setRaining(false);
                    worldinfo.setThundering(false);
                    notifyCommandListener(sender, this, "commands.weather.clear");
                }
                else if ("rain".equalsIgnoreCase(args[1]))
                {
                    worldinfo.setCleanWeatherTime(0);
                    worldinfo.setRainTime(i);
                    worldinfo.setThunderTime(i);
                    worldinfo.setRaining(true);
                    worldinfo.setThundering(false);
                    notifyCommandListener(sender, this, "commands.weather.rain");
                }
                else
                {
                    if (!"thunder".equalsIgnoreCase(args[1]))
                    {
                        throw new WrongUsageException("instances.commands.weatherd.usage");
                    }

                    worldinfo.setCleanWeatherTime(0);
                    worldinfo.setRainTime(i);
                    worldinfo.setThunderTime(i);
                    worldinfo.setRaining(true);
                    worldinfo.setThundering(true);
                    notifyCommandListener(sender, this, "commands.weather.thunder");
                }
            }
            else
            {
                notifyCommandListener(sender, this, "No dimension found with the id %s", dimension);
            }
        }
        else
        {
            throw new WrongUsageException("instances.commands.weatherd.usage");
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 2 ? getListOfStringsMatchingLastWord(args, "clear", "rain", "thunder") : null;
    }
}
