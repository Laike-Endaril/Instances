package com.fantasticsource.instances.world.dimensions;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.world.dimensions.libraryofworlds.WorldProviderLibraryOfWorlds;
import com.fantasticsource.instances.world.dimensions.roomtile.WorldProviderRoomTile;
import com.fantasticsource.instances.world.dimensions.skyroom.WorldProviderSkyroom;
import com.fantasticsource.instances.world.dimensions.template.WorldProviderTemplate;
import com.fantasticsource.instances.world.dimensions.voided.WorldProviderVoid;
import net.minecraft.world.DimensionType;

public class InstanceTypes
{
    public static final DimensionType
            VOID = DimensionType.register("Void", "_void", Instances.nextFreeDimTypeID(), WorldProviderVoid.class, false),
            SKYROOM = DimensionType.register("Skyroom", "_skyroom", Instances.nextFreeDimTypeID(), WorldProviderSkyroom.class, false),
            LIBRARY_OF_WORLDS = DimensionType.register("Library_of_Worlds", "_library_of_worlds", Instances.nextFreeDimTypeID(), WorldProviderLibraryOfWorlds.class, false),
            TEMPLATE = DimensionType.register("Template", "_template", Instances.nextFreeDimTypeID(), WorldProviderTemplate.class, false),
            ROOM_TILE = DimensionType.register("Room_Tile", "_room_tile", Instances.nextFreeDimTypeID(), WorldProviderRoomTile.class, false);

    public static void init()
    {
        //Indirectly initializes the fields above
    }
}
