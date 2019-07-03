package com.fantasticsource.instances.network.handler;

import com.fantasticsource.instances.network.messages.MessageCreateDimension;
import com.fantasticsource.instances.server.InstanceHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HandlerCreateDimension implements IMessageHandler<MessageCreateDimension, IMessage>
{
    @Override
    public IMessage onMessage(final MessageCreateDimension message, final MessageContext ctx)
    {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() ->
        {
            NetHandlerPlayServer netHandler = (NetHandlerPlayServer) ctx.netHandler;

            if (netHandler.player.canUseCommand(3, "instances"))
            {
                InstanceHandler.createDimension(((NetHandlerPlayServer) ctx.netHandler).player, message.getWorldInfo());
            }
        });

        return null;
    }
}
