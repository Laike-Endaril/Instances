package com.fantasticsource.instances.dimensions.voided;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class BiomeVoid extends Biome
{
    public static Biome voidBiome;

    public BiomeVoid()
    {
        super(new BiomeProperties("Void"));
    }

    @Override
    public List<SpawnListEntry> getSpawnableList(EnumCreatureType creatureType)
    {
        return new ArrayList<>();
    }

    public static void init()
    {
        voidBiome = new BiomeVoid();
    }
}
