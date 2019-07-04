package com.fantasticsource.instances.network.messages;

import com.fantasticsource.instances.world.WorldInfoSimple;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageCreateDimension implements IMessage
{
    WorldInfoSimple worldInfo;

    public MessageCreateDimension()
    {
        //Required IIRC (Laike_Endaril)
    }

    public MessageCreateDimension(WorldInfoSimple worldInfo)
    {
        this.worldInfo = worldInfo;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.worldInfo = new WorldInfoSimple(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, worldInfo.cloneNBTCompound(null));
    }

    public WorldInfoSimple getWorldInfo()
    {
        return worldInfo;
    }

}
