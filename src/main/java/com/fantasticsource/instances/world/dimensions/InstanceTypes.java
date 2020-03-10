package com.fantasticsource.instances.world.dimensions;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.dimensions.libraryofworlds.WorldProviderLibraryOfWorlds;
import com.fantasticsource.instances.world.dimensions.skyroom.WorldProviderSkyroom;
import com.fantasticsource.instances.world.dimensions.voided.WorldProviderVoid;
import com.fantasticsource.tools.Tools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;

import java.io.File;

public class InstanceTypes
{
    public static DimensionType voidDimType = DimensionType.register("Void", "_void", Instances.nextFreeDimTypeID(), WorldProviderVoid.class, false);
    public static DimensionType skyroomDimType = DimensionType.register("Skyroom", "_skyroom", Instances.nextFreeDimTypeID(), WorldProviderSkyroom.class, false);
    public static DimensionType libraryOfWorldsDimType = DimensionType.register("Library of Worlds", "_library_of_worlds", Instances.nextFreeDimTypeID(), WorldProviderLibraryOfWorlds.class, false);

    public static DimensionType[] instanceTypes = new DimensionType[]
            {
                    voidDimType,
                    skyroomDimType,
                    libraryOfWorldsDimType
            };

    public static void init()
    {
        //Indirectly initializes the fields above
    }

    public static String getInstanceTypeDir(MinecraftServer server, DimensionType instanceType)
    {
        if (!Tools.contains(instanceTypes, instanceType)) throw new IllegalArgumentException("Invalid instance type: " + instanceType);

        return InstanceHandler.getInstancesDir(server) + instanceType.getName().replaceAll(" ", "_") + File.separator;
    }
}
