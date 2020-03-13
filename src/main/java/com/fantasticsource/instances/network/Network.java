package com.fantasticsource.instances.network;

import com.fantasticsource.instances.InstanceData;
import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.client.LocalDimensions;
import com.fantasticsource.instances.client.gui.PersonalPortalGUI;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.tags.savefile.Owners;
import com.fantasticsource.instances.tags.savefile.Visitors;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.PlayerData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        WRAPPER.registerMessage(SyncInstancesPacketHandler.class, SyncDimensionTypePacket.class, discriminator++, Side.CLIENT);

        WRAPPER.registerMessage(PersonalPortalGUIPacketHandler.class, PersonalPortalGUIPacket.class, discriminator++, Side.CLIENT);
        WRAPPER.registerMessage(PersonalPortalPacketHandler.class, PersonalPortalPacket.class, discriminator++, Side.SERVER);
    }


    public static class SyncDimensionTypePacket implements IMessage
    {
        public int dimension;
        public String dimensionTypeName;

        public SyncDimensionTypePacket()
        {
        }

        public SyncDimensionTypePacket(int dimension)
        {
            this.dimension = dimension;
            this.dimensionTypeName = DimensionManager.getProviderType(dimension).getName();
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(dimension);
            ByteBufUtils.writeUTF8String(buf, dimensionTypeName);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            dimension = buf.readInt();
            dimensionTypeName = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class SyncInstancesPacketHandler implements IMessageHandler<SyncDimensionTypePacket, IMessage>
    {
        @Override
        public IMessage onMessage(SyncDimensionTypePacket message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() -> LocalDimensions.sync(message));
            return null;
        }
    }


    public static class PersonalPortalGUIPacket implements IMessage
    {
        public String[] names;
        public boolean isInInstance, isInOwnedSkyroom;
        EntityPlayerMP player;

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
            InstanceData data = InstanceData.get(player);

            buf.writeBoolean(data != null);
            buf.writeBoolean(data != null && data.getDimensionType() == InstanceTypes.SKYROOM && ("" + player.getPersistentID()).equals(data.getOwner()));

            names = Visitors.visitableInstances(FMLCommonHandler.instance().getMinecraftServerInstance(), player.getPersistentID());
            buf.writeInt(names.length);
            for (String id : names) ByteBufUtils.writeUTF8String(buf, PlayerData.getName(UUID.fromString(id)));
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            isInInstance = buf.readBoolean();
            isInOwnedSkyroom = buf.readBoolean();

            int size = buf.readInt();
            names = new String[size];
            for (int i = 0; i < size; i++)
            {
                names[i] = ByteBufUtils.readUTF8String(buf);
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

                InstanceData data;
                switch (s)
                {
                    case "Leave Instance":
                        Teleport.escape(player);
                        return;

                    case "Go Home":
                        data = InstanceData.get(true, InstanceTypes.SKYROOM, "" + player.getPersistentID());

                        Owners.setOwner(server, data.getFullName(), "" + player.getPersistentID());
                        Teleport.joinPossiblyCreating(player, data.getFullName());
                        return;

                    default:
                        data = InstanceData.get(true, InstanceTypes.SKYROOM, s);
                        if (data == null || !data.canVisit(player.getPersistentID())) return;

                        Owners.setOwner(server, data.getFullName(), s);
                        Teleport.joinPossiblyCreating(player, data.getFullName());
                }
            });
            return null;
        }
    }
}
