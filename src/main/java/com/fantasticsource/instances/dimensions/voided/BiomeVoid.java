package com.fantasticsource.instances.dimensions.voided;

import net.minecraft.world.biome.Biome;

public class BiomeVoid extends Biome
{
    public static Biome voidBiome;

    public BiomeVoid()
    {
        super(new BiomeProperties("Void"));
    }

    public static void init()
    {
        voidBiome = new BiomeVoid();
    }
}
