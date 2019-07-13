package com.fantasticsource.instances.network;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.client.gui.PersonalPortalGUI;
import com.fantasticsource.instances.network.handler.CreateInstancePacketHandler;
import com.fantasticsource.instances.network.handler.OpenCreationGUIPacketHandler;
import com.fantasticsource.instances.network.handler.SyncInstancesPacketHandler;
import com.fantasticsource.instances.network.messages.CreateInstancePacket;
import com.fantasticsource.instances.network.messages.OpenCreationGUIPacket;
import com.fantasticsource.instances.network.messages.SyncInstancesPacket;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.dimensions.libraryofworlds.VisitablePlayersData;
import com.fantasticsource.mctools.PlayerData;
import com.fantasticsource.tools.datastructures.SortableTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Network
{
    public static final SimpleNetworkWrapper WRAPPER = new SimpleNetworkWrapper(Instances.MODID);
    private static final LinkedHashMap<EntityPlayerMP, World> personalPortalWorlds = new LinkedHashMap<>();
    private static final LinkedHashMap<EntityPlayerMP, BlockPos> personalPortalPositions = new LinkedHashMap<>();
    private static int discriminator = 0;

    public static void init()
    {
        WRAPPER.registerMessage(CreateInstancePacketHandler.class, CreateInstancePacket.class, discriminator++, Side.SERVER);
        WRAPPER.registerMessage(OpenCreationGUIPacketHandler.class, OpenCreationGUIPacket.class, discriminator++, Side.CLIENT);
        WRAPPER.registerMessage(SyncInstancesPacketHandler.class, SyncInstancesPacket.class, discriminator++, Side.CLIENT);

        WRAPPER.registerMessage(PersonalPortalGUIPacketHandler.class, PersonalPortalGUIPacket.class, discriminator++, Side.CLIENT);
        WRAPPER.registerMessage(PersonalPortalPacketHandler.class, PersonalPortalPacket.class, discriminator++, Side.SERVER);
    }


    public static class PersonalPortalGUIPacket implements IMessage
    {
        EntityPlayerMP player;
        ArrayList<String> namesIn;
        String[] namesOut;
        boolean isInInstance;

        public PersonalPortalGUIPacket()
        {
            //Required
        }

        public PersonalPortalGUIPacket(EntityPlayerMP player)
        {
            this.player = player;
            personalPortalWorlds.put(player, player.world);
            personalPortalPositions.put(player, player.getPosition());
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeBoolean(InstanceHandler.instanceInfo.keySet().contains(player.dimension));

            InstanceWorldInfo info = InstanceHandler.get(player.dimension);
            String ownername = info == null ? null : PlayerData.getName(info.getOwner());
            namesIn = new ArrayList<>();
            VisitablePlayersData data = InstanceHandler.visitablePlayersData.get(player.getPersistentID());
            if (data != null)
            {
                Object[] nameTables = data.visitablePlayers.getColumn(1);
                for (Object nameTable : nameTables)
                {
                    for (Object name : ((SortableTable) nameTable).getColumn(0))
                    {
                        if (!name.equals(ownername)) namesIn.add((String) name);
                    }
                }
            }

            buf.writeInt(namesIn.size());
            for (String name : namesIn) ByteBufUtils.writeUTF8String(buf, name);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            isInInstance = buf.readBoolean();

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
                PersonalPortalGUI.isInInstance = message.isInInstance;
                PersonalPortalGUI.personalPortalGUI.initGui();
                mc.displayGuiScreen(PersonalPortalGUI.personalPortalGUI);
            });
            return null;
        }
    }


    public static class PersonalPortalPacket implements IMessage
    {
        String selection;

        public PersonalPortalPacket()
        {
            //Required
        }

        public PersonalPortalPacket(String selection)
        {
            this.selection = selection;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            ByteBufUtils.writeUTF8String(buf, selection);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            selection = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class PersonalPortalPacketHandler implements IMessageHandler<PersonalPortalPacket, IMessage>
    {
        @Override
        public IMessage onMessage(PersonalPortalPacket message, MessageContext ctx)
        {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() ->
            {
                EntityPlayerMP player = ctx.getServerHandler().player;
                if (player.world != personalPortalWorlds.get(player)) return;
                if (personalPortalPositions.get(player).distanceSq(player.getPosition()) > 9) return;

                String s = message.selection;
                if (s == null) return;

                switch (s)
                {
                    case "Leave Instance":
                        Teleport.escape(player);
                        return;

                    case "Go Home":
                        Teleport.joinPossiblyCreating(player);
                        return;

                    default:
                        VisitablePlayersData data = InstanceHandler.visitablePlayersData.get(player.getPersistentID());
                        if (data == null) return;

                        SortableTable nameTable = (SortableTable) data.visitablePlayers.get(0, s.charAt(0), 1);
                        if (nameTable == null || !nameTable.contains(s, 0)) return;

                        Teleport.joinPossiblyCreating(player, s);
                }
            });
            return null;
        }
    }
}