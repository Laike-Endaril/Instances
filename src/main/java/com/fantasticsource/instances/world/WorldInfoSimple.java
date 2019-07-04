package com.fantasticsource.instances.world;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.*;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

public class WorldInfoSimple extends WorldInfo
{
    private WorldInfo superInfo;
    private DimensionType dimensionType = DimensionType.OVERWORLD;
    private UUID owner = null;

    public WorldInfoSimple(NBTTagCompound nbt)
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

        superInfo = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getWorldInfo();
    }

    public WorldInfoSimple(WorldSettings settings, String name, DimensionType dimType)
    {
        super(settings, name);
        dimensionType = dimType != null ? dimType : DimensionType.OVERWORLD;
    }

    @Override
    public NBTTagCompound cloneNBTCompound(@Nullable NBTTagCompound nbt)
    {
        NBTTagCompound superNbt = super.cloneNBTCompound(nbt);
        superNbt.setString("dimType", dimensionType.getName());
        superNbt.setString("owner", owner == null ? "null" : owner.toString());
        return superNbt;
    }

    @Override
    public NBTTagCompound getPlayerNBTTagCompound()
    {
        return superInfo.getPlayerNBTTagCompound();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public long getLastTimePlayed()
    {
        return superInfo.getLastTimePlayed();
    }

    @Override
    public GameType getGameType()
    {
        return superInfo.getGameType();
    }

    @Override
    public boolean isHardcoreModeEnabled()
    {
        return superInfo.isHardcoreModeEnabled();
    }

    @Override
    public boolean areCommandsAllowed()
    {
        return superInfo.areCommandsAllowed();
    }

    @Override
    public GameRules getGameRulesInstance()
    {
        return superInfo.getGameRulesInstance();
    }

    @Override
    public EnumDifficulty getDifficulty()
    {
        return superInfo.getDifficulty();
    }

    @Override
    public boolean isDifficultyLocked()
    {
        return superInfo.isDifficultyLocked();
    }

    public DimensionType getDimensionType()
    {
        return dimensionType;
    }

    public void setOwner(UUID id)
    {
        owner = id;
    }

    public UUID getOwner()
    {
        return owner;
    }

    public void setOwner(EntityPlayerMP player)
    {
        setOwner(player.getPersistentID());
    }
}