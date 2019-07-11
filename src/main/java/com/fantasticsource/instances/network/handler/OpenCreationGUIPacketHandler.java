package com.fantasticsource.instances.network.handler;

import com.fantasticsource.instances.client.oldgui.GuiCreateDimension;
import com.fantasticsource.instances.network.messages.OpenCreationGUIPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OpenCreationGUIPacketHandler implements IMessageHandler<OpenCreationGUIPacket, IMessage>
{
    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(OpenCreationGUIPacket message, MessageContext ctx)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new GuiCreateDimension()));
        return null;
    }
}
