package com.fantasticsource.instances.network.handler;

import com.fantasticsource.instances.client.gui.GuiCreateDimension;
import com.fantasticsource.instances.network.messages.MessageOpenGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandlerOpenGui implements IMessageHandler<MessageOpenGui, IMessage>
{
    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageOpenGui message, MessageContext ctx)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new GuiCreateDimension()));
        return null;
    }
}
