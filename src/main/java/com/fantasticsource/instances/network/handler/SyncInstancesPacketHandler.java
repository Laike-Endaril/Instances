package com.fantasticsource.instances.network.handler;

import com.fantasticsource.instances.client.ClientHandler;
import com.fantasticsource.instances.network.messages.SyncInstancesPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncInstancesPacketHandler implements IMessageHandler<SyncInstancesPacket, IMessage>
{
    @Override
    public IMessage onMessage(SyncInstancesPacket message, MessageContext ctx)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> ClientHandler.sync(message.getDimensions()));
        return null;
    }
}
