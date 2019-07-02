package com.fantasticsource.instances.instancetypes.voided;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;

import java.util.ArrayList;
import java.util.List;

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
        BiomeManager.addSpawnBiome(voidBiome);
    }

    @Override
    public List<SpawnListEntry> getSpawnableList(EnumCreatureType creatureType)
    {
        return new ArrayList<>();
    }
}
