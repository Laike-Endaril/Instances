package com.fantasticsource.instances.world.dimensions.voided;

import com.fantasticsource.instances.world.InstanceWorldProvider;
import com.fantasticsource.instances.world.boimes.BiomeProviders;
import com.fantasticsource.instances.world.boimes.BiomeVoid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderVoid extends InstanceWorldProvider
{
    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorVoid(world);
    }

    @Override
    public Biome getBiomeForCoords(BlockPos pos)
    {
        return BiomeVoid.voidBiome;
    }

    @Override
    public BiomeProvider getBiomeProvider()
    {
        return BiomeProviders.VOID;
    }
}
