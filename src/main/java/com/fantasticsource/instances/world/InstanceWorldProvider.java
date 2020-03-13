package com.fantasticsource.instances.world;

import com.fantasticsource.instances.InstanceData;
import com.fantasticsource.tools.Tools;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;

public abstract class InstanceWorldProvider extends WorldProvider
{
    protected static boolean XAEROS;

    public final InstanceData data;

    public InstanceWorldProvider(InstanceData data)
    {
        XAEROS = Loader.isModLoaded("xaeroworldmap");

        this.data = data;
    }

    @Nullable
    @Override
    public final String getSaveFolder()
    {
        //If you return null here, it saves to the overworld

        if (XAEROS && world.isRemote && Tools.stackContainsSubstring("xaero")) return "instancesInstance";

        return data.getFullName();
    }

    @Override
    public final DimensionType getDimensionType()
    {
        return data.getDimensionType();
    }
}
