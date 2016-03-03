package lumien.simpledimensions.network.handler;

import lumien.simpledimensions.dimensions.DimensionHandler;
import lumien.simpledimensions.network.messages.MessageCreateDimension;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HandlerCreateDimension implements IMessageHandler<MessageCreateDimension, IMessage>
{

	@Override
	public IMessage onMessage(final MessageCreateDimension message, final MessageContext ctx)
	{
		MinecraftServer.getServer().addScheduledTask(new Runnable()
		{
			@Override
			public void run()
			{
				NetHandlerPlayServer netHandler = (NetHandlerPlayServer) ctx.netHandler;

				if (netHandler.playerEntity.canCommandSenderUseCommand(3, "simpledimensions"))
				{
					DimensionHandler.getInstance().createDimension(((NetHandlerPlayServer) ctx.netHandler).playerEntity, message.getWorldInfo());
				}
			}
		});

		return null;
	}

}
