package com.fantasticsource.instances.network;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.client.gui.PersonalPortalGUI;
import com.fantasticsource.instances.network.handler.SyncInstancesPacketHandler;
import com.fantasticsource.instances.network.messages.SyncInstancesPacket;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.tags.world.SkyroomVisitors;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.PlayerData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
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
import java.util.UUID;

public class Network
{
    public static final SimpleNetworkWrapper WRAPPER = new SimpleNetworkWrapper(Instances.MODID);
    private static final LinkedHashMap<EntityPlayerMP, World> personalPortalWorlds = new LinkedHashMap<>();
    private static final LinkedHashMap<EntityPlayerMP, BlockPos> personalPortalPositions = new LinkedHashMap<>();
    private static int discriminator = 0;

    public static void init()
    {
        WRAPPER.registerMessage(SyncInstancesPacketHandler.class, SyncInstancesPacket.class, discriminator++, Side.CLIENT);

        WRAPPER.registerMessage(PersonalPortalGUIPacketHandler.class, PersonalPortalGUIPacket.class, discriminator++, Side.CLIENT);
        WRAPPER.registerMessage(PersonalPortalPacketHandler.class, PersonalPortalPacket.class, discriminator++, Side.SERVER);
    }


    public static class PersonalPortalGUIPacket implements IMessage
    {
        public String[] namesOut;
        public boolean isInInstance, isInOwnedSkyroom;
        EntityPlayerMP player;
        ArrayList<String> namesIn;

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
            InstanceWorldInfo info = InstanceHandler.get(player.dimension);

            buf.writeBoolean(info != null);
            buf.writeBoolean(info != null && info.getDimensionType() == InstanceTypes.skyroomDimType && player.getPersistentID().equals(info.getOwner()));

            namesIn = SkyroomVisitors.visitables(FMLCommonHandler.instance().getMinecraftServerInstance(), player.getPersistentID());
            buf.writeInt(namesIn.size());
            for (String id : namesIn) ByteBufUtils.writeUTF8String(buf, PlayerData.getName(UUID.fromString(id)));
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            isInInstance = buf.readBoolean();
            isInOwnedSkyroom = buf.readBoolean();

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
        public IMessage onMessage(PersonalPortalGUIPacket packet, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() -> PersonalPortalGUI.show(packet));
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
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            server.addScheduledTask(() ->
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
                        Teleport.joinPossiblyCreating(player, "" + player.getPersistentID(), InstanceTypes.skyroomDimType, player.getPersistentID(), true);
                        return;

                    default:
                        PlayerData data = PlayerData.get(s);
                        if (data == null || !SkyroomVisitors.canVisit(server, player.getPersistentID(), data.id)) return;

                        Teleport.joinPossiblyCreating(player, "" + data.id, InstanceTypes.skyroomDimType, data.id, true);
                }
            });
            return null;
        }
    }
}
