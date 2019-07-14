package com.fantasticsource.instances.network.handler;

import com.fantasticsource.instances.network.messages.CreateInstancePacket;
import com.fantasticsource.instances.world.InstanceHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CreateInstancePacketHandler implements IMessageHandler<CreateInstancePacket, IMessage>
{
    @Override
    public IMessage onMessage(final CreateInstancePacket message, final MessageContext ctx)
    {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() ->
        {
            NetHandlerPlayServer netHandler = (NetHandlerPlayServer) ctx.netHandler;

            if (netHandler.player.canUseCommand(3, "instances"))
            {
                InstanceHandler.createInstance(((NetHandlerPlayServer) ctx.netHandler).player, message.getWorldInfo());
            }
        });

        return null;
    }
}
