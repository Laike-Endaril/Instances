package com.fantasticsource.instances.world;

import com.fantasticsource.instances.Instances;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class InstanceWorldInfo extends WorldInfo
{
    public ArrayList<UUID> visitorWhitelist = new ArrayList<>();
    private DimensionType dimensionType = null;
    private UUID owner = null;

    public InstanceWorldInfo(NBTTagCompound nbt)
    {
        super(nbt);

        try
        {
            dimensionType = DimensionType.byName(nbt.getString("dimType"));
        }
        catch (IllegalArgumentException e)
        {
            //Just keep default value from above
        }

        try
        {
            owner = UUID.fromString(nbt.getString("owner"));
        }
        catch (IllegalArgumentException e)
        {
            //Just keep default value from above
        }

        NBTTagList list = nbt.getTagList("visitorWhitelist", Constants.NBT.TAG_STRING);
        for (NBTBase tag : list)
        {
            visitorWhitelist.add(UUID.fromString(((NBTTagString) tag).getString()));
        }
    }

    public InstanceWorldInfo(WorldSettings settings, String name, DimensionType dimType)
    {
        super(settings, name);
        dimensionType = dimType != null ? dimType : DimensionType.OVERWORLD;
    }

    @Override
    public NBTTagCompound cloneNBTCompound(@Nullable NBTTagCompound nbt)
    {
        NBTTagCompound result = super.cloneNBTCompound(nbt);

        result.setString("dimType", dimensionType.getName());
        result.setString("owner", owner == null ? "null" : owner.toString());

        NBTTagList list = new NBTTagList();
        for (UUID id : visitorWhitelist) list.appendTag(new NBTTagString(id.toString()));
        result.setTag("visitorWhitelist", list);

        return result;
    }

    public DimensionType getDimensionType()
    {
        return dimensionType;
    }

    public void setDimensionType(DimensionType dimensionType)
    {
        this.dimensionType = dimensionType;
    }

    public void setOwner(EntityPlayerMP player)
    {
        setOwner(player.getPersistentID());
    }

    public UUID getOwner()
    {
        return owner;
    }

    public void setOwner(UUID id)
    {
        owner = id;

        for (Map.Entry<Integer, InstanceWorldInfo> entry : InstanceHandler.instanceInfo.entrySet())
        {
            if (entry.getValue() == this)
            {
                World world = DimensionManager.getWorld(entry.getKey());
                if (world == null) break;

                for (EntityPlayer player : world.playerEntities)
                {
                    Instances.setPlayerMode((EntityPlayerMP) player, this);
                }
            }
        }
    }
}
