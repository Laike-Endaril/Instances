package com.fantasticsource.instances.network.messages;

import com.fantasticsource.instances.world.InstanceWorldInfo;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CreateInstancePacket implements IMessage
{
    InstanceWorldInfo worldInfo;

    public CreateInstancePacket()
    {
        //Required IIRC (Laike_Endaril)
    }

    public CreateInstancePacket(InstanceWorldInfo worldInfo)
    {
        this.worldInfo = worldInfo;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.worldInfo = new InstanceWorldInfo(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, worldInfo.cloneNBTCompound(null));
    }

    public InstanceWorldInfo getWorldInfo()
    {
        return worldInfo;
    }

}
