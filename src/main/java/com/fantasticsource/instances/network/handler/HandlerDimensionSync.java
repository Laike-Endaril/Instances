package com.fantasticsource.instances.network.handler;

import com.fantasticsource.instances.client.ClientHandler;
import com.fantasticsource.instances.network.messages.MessageDimensionSync;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HandlerDimensionSync implements IMessageHandler<MessageDimensionSync, IMessage>
{
    @Override
    public IMessage onMessage(MessageDimensionSync message, MessageContext ctx)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> ClientHandler.sync(message.getDimensions()));
        return null;
    }
}
