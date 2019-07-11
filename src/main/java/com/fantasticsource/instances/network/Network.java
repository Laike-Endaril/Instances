package com.fantasticsource.instances.network;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.client.gui.PersonalPortalGUI;
import com.fantasticsource.instances.network.handler.CreateInstancePacketHandler;
import com.fantasticsource.instances.network.handler.OpenCreationGUIPacketHandler;
import com.fantasticsource.instances.network.handler.SyncInstancesPacketHandler;
import com.fantasticsource.instances.network.messages.CreateInstancePacket;
import com.fantasticsource.instances.network.messages.OpenCreationGUIPacket;
import com.fantasticsource.instances.network.messages.SyncInstancesPacket;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.tools.datastructures.SortableTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class Network
{
    public static final SimpleNetworkWrapper WRAPPER = new SimpleNetworkWrapper(Instances.MODID);
    private static int discriminator = 0;

    public static void init()
    {
        WRAPPER.registerMessage(CreateInstancePacketHandler.class, CreateInstancePacket.class, discriminator++, Side.SERVER);

        WRAPPER.registerMessage(OpenCreationGUIPacketHandler.class, OpenCreationGUIPacket.class, discriminator++, Side.CLIENT);
        WRAPPER.registerMessage(SyncInstancesPacketHandler.class, SyncInstancesPacket.class, discriminator++, Side.CLIENT);

        WRAPPER.registerMessage(PersonalPortalGUIPacketHandler.class, PersonalPortalGUIPacket.class, discriminator++, Side.CLIENT);
    }

    public static class PersonalPortalGUIPacket implements IMessage
    {
        EntityPlayerMP player;
        ArrayList<String> namesIn;
        String[] namesOut;

        public PersonalPortalGUIPacket()
        {
            //Required
        }

        public PersonalPortalGUIPacket(EntityPlayerMP player)
        {
            this.player = player;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            namesIn = new ArrayList<>();
            Object[] nameTables = InstanceHandler.visitablePlayersData.get(player.getPersistentID()).visitablePlayers.getColumn(1);
            for (Object nameTable : nameTables)
            {
                for (Object name : ((SortableTable) nameTable).getColumn(0))
                {
                    namesIn.add((String) name);
                }
            }

            buf.writeInt(namesIn.size());
            for (String name : namesIn) ByteBufUtils.writeUTF8String(buf, name);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            int size = buf.readInt();
            namesOut = new String[size];
            for (int i = 0; i < size; i++)
            {
                namesOut[i] = ByteBufUtils.readUTF8String(buf);
            }
        }
    }

    public static class PersonalPortalGUIPacketHandler implements IMessageHandler<PersonalPortalGUIPacket, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PersonalPortalGUIPacket message, MessageContext ctx)
        {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() ->
            {
                PersonalPortalGUI.names = message.namesOut;
                PersonalPortalGUI.personalPortalGUI.initGui();
                mc.displayGuiScreen(PersonalPortalGUI.personalPortalGUI);
            });
            return null;
        }
    }
}
