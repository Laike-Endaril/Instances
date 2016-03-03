package lumien.simpledimensions.server.commands;

import java.util.List;
import lumien.simpledimensions.dimensions.DimensionHandler;
import lumien.simpledimensions.network.PacketHandler;
import lumien.simpledimensions.network.messages.MessageOpenGui;
import lumien.simpledimensions.util.TeleporterSimple;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

public class CommandSimpleDimensions extends CommandBase
{

	@Override
	public String getCommandName()
	{
		return "simpledimensions";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "Usage: /simpledimensions <create:delete:list>";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 3;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length == 0)
		{
			sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
			return;
		}

		if (args[0].equals("create"))
		{
			if (sender instanceof EntityPlayerMP && !(sender instanceof FakePlayer))
			{
				EntityPlayerMP player = (EntityPlayerMP) sender;
				
				PacketHandler.INSTANCE.sendTo(new MessageOpenGui().setGuiID(0), player);
			}
		}
		else if (args[0].equals("delete"))
		{
			if (args.length == 2)
			{
				int dimensionID = Integer.parseInt(args[1]);

				DimensionHandler.getInstance().deleteDimension(sender, dimensionID);
			}
		}
		else if (args[0].equals("list"))
		{
			sender.addChatMessage(DimensionHandler.getInstance().generateList());
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
	{
		if (args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args, "create", "delete", "list");
		}
		else
		{
			return null;
		}
	}
}
