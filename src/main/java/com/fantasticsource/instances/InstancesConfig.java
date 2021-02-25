package com.fantasticsource.instances;

import net.minecraftforge.common.config.Config;

import static com.fantasticsource.instances.Instances.MODID;

@Config(modid = MODID)
public class InstancesConfig
{
    @Config.Name("Celestial Angle Paths")
    @Config.LangKey(MODID + ".config.celestialAnglePaths")
    @Config.Comment(
            {
                    "Paths used for movement of the sky in instances",
                    "Syntax is...",
                    "InstanceNameRegexFilter, PathNBTString"
            })
    public static String[] celestialAnglePaths = new String[0];
}
