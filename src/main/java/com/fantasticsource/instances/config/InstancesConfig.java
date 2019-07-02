package com.fantasticsource.instances.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class InstancesConfig
{
    private int startDimensionID;

    public void preInit(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        startDimensionID = config.get("Settings", "StartDimensionID", Integer.MIN_VALUE, "Where should Instances start to search for free Dimension IDs?").getInt();

        if (config.hasChanged()) config.save();
    }

    public int startDimensionID()
    {
        return startDimensionID;
    }
}
