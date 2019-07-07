package com.fantasticsource.instances.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashMap;
import java.util.Map;

public class MessageDimensionSync implements IMessage
{
    private HashMap<Integer, DimensionType> instances;

    public MessageDimensionSync()
    {
        instances = new HashMap<>();
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        for (String key : tag.getKeySet())
        {
            int dimID;
            DimensionType dimType = DimensionType.OVERWORLD;
            try
            {
                dimID = Integer.parseInt(key);
            }
            catch (NumberFormatException e)
            {
                continue;
            }

            String dimTypeS = tag.getString(key);
            if (dimTypeS != null)
            {
                try
                {
                    dimType = DimensionType.byName(dimTypeS);
                }
                catch (IllegalArgumentException e)
                {
                }
            }

            instances.put(dimID, dimType);
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<Integer, DimensionType> entry : instances.entrySet())
        {
            tag.setString(Integer.toString(entry.getKey()), entry.getValue().getName());
        }
        ByteBufUtils.writeTag(buf, tag);
    }

    public void addDimension(int id, DimensionType type)
    {
        if (type == null)
        {
            type = DimensionType.OVERWORLD;
        }
        instances.put(id, type);
    }

    public HashMap<Integer, DimensionType> getDimensions()
    {
        return instances;
    }
}
