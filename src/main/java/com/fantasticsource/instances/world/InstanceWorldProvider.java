package com.fantasticsource.instances.world;

import com.fantasticsource.tools.Tools;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.io.File;

public abstract class InstanceWorldProvider extends WorldProvider
{
    protected static boolean XAEROS;

    public InstanceWorldProvider()
    {
        XAEROS = Loader.isModLoaded("xaeroworldmap");
    }

    @Nullable
    @Override
    public final String getSaveFolder()
    {
        //If you return null here, it saves to the overworld

        if (XAEROS && world.isRemote && Tools.stackContainsSubstring("xaero")) return "instancesInstance";

        return "Instances" + File.separator + world.getWorldInfo().getWorldName();
    }

    @Override
    public final DimensionType getDimensionType()
    {
        DimensionType type = DimensionType.OVERWORLD;

        try
        {
            type = DimensionManager.getProviderType(getDimension());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }

        return type;
    }
}
