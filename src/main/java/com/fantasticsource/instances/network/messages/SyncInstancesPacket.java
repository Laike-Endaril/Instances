package com.fantasticsource.instances.network.messages;

import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SyncInstancesPacket implements IMessage
{
    public HashMap<Integer, DimensionType> instances;

    @Override
    public void toBytes(ByteBuf buf)
    {
        Set<Map.Entry<Integer, InstanceWorldInfo>> entries = InstanceHandler.instanceInfo.entrySet();
        buf.writeInt(entries.size());

        for (Map.Entry<Integer, InstanceWorldInfo> entry : entries)
        {
            buf.writeInt(entry.getKey());
            ByteBufUtils.writeUTF8String(buf, entry.getValue().getDimensionType().getName());
        }
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        int size = buf.readInt();
        instances = new HashMap<>(size);

        for (int i = size; i > 0; i--)
        {
            instances.put(buf.readInt(), DimensionType.byName(ByteBufUtils.readUTF8String(buf)));
        }
    }
}
