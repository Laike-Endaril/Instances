package com.fantasticsource.instances.dimension;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.dimension.skyroom.WorldProviderSkyroom;
import com.fantasticsource.instances.dimension.voided.WorldProviderVoid;
import net.minecraft.world.DimensionType;

public class InstanceTypes
{
    public static DimensionType voidDimType = DimensionType.register("Void", "_void", Instances.nextFreeDimTypeID(), WorldProviderVoid.class, false);
    public static DimensionType skyroomDimType = DimensionType.register("Skyroom", "_skyroom", Instances.nextFreeDimTypeID(), WorldProviderSkyroom.class, false);

    public static void init()
    {
        //Indirectly initializes the fields above
    }
}
