package lumien.simpledimensions.server.commands;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class CommandTimeD extends CommandBase
{
	private static final String __OBFID = "CL_00001183";

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
		return "simpleDimensions.commands.timed.usage";
	}

	@Override
	public void execute(MinecraftServer server,ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length > 2)
		{
			int dimension = Integer.parseInt(args[0]);
			int i;

			if (args[1].equals("set"))
			{
				if (args[2].equals("day"))
				{
					i = 1000;
				}
				else if (args[2].equals("night"))
				{
					i = 13000;
				}
				else
				{
					i = parseInt(args[2], 0);
				}

				this.setTime(sender, dimension, i);
				notifyCommandListener(sender, this, "commands.time.set", new Object[] { Integer.valueOf(i) });
				return;
			}

			if (args[1].equals("add"))
			{
				i = parseInt(args[2], 0);
				this.addTime(sender, dimension, i);
				notifyCommandListener(sender, this, "commands.time.added", new Object[] { Integer.valueOf(i) });
				return;
			}

			if (args[1].equals("query"))
			{
				if (args[2].equals("daytime"))
				{
					i = (int) (sender.getEntityWorld().getWorldTime() % 2147483647L);
					sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, i);
					notifyCommandListener(sender, this, "commands.time.query", new Object[] { Integer.valueOf(i) });
					return;
				}

				if (args[2].equals("gametime"))
				{
					i = (int) (sender.getEntityWorld().getTotalWorldTime() % 2147483647L);
					sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, i);
					notifyCommandListener(sender, this, "commands.time.query", new Object[] { Integer.valueOf(i) });
					return;
				}
			}
		}

		throw new WrongUsageException("simpleDimensions.commands.timed.usage", new Object[0]);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
		return args.length == 2 ? getListOfStringsMatchingLastWord(args, new String[] { "set", "add", "query" }) : (args.length == 3 && args[1].equals("set") ? getListOfStringsMatchingLastWord(args, new String[] { "day", "night" }) : (args.length == 3 && args[1].equals("query") ? getListOfStringsMatchingLastWord(args, new String[] { "daytime", "gametime" }) : null));
	}

	/**
	 * Set the time in the server object.
	 */
	protected void setTime(ICommandSender p_71552_1_, int dimension, int p_71552_2_)
	{
		WorldServer worldserver = DimensionManager.getWorld(dimension);
		worldserver.setWorldTime(p_71552_2_);
	}

	/**
	 * Adds (or removes) time in the server object.
	 */
	protected void addTime(ICommandSender p_71553_1_, int dimension, int p_71553_2_)
	{
		WorldServer worldserver = DimensionManager.getWorld(dimension);
		worldserver.setWorldTime(worldserver.getWorldTime() + p_71553_2_);
	}
}