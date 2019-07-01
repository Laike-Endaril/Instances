package lumien.simpledimensions.network.handler;

import lumien.simpledimensions.client.gui.GuiCreateDimension;
import lumien.simpledimensions.network.messages.MessageOpenGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HandlerOpenGui implements IMessageHandler<MessageOpenGui, IMessage>
{
    @Override
    public IMessage onMessage(MessageOpenGui message, MessageContext ctx)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new GuiCreateDimension()));
        return null;
    }
}
