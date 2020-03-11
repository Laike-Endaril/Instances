package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.instances.world.InstanceWorldProvider;
import com.fantasticsource.instances.world.boimes.BiomeProviders;
import com.fantasticsource.instances.world.boimes.BiomeVoid;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;

import javax.annotation.Nullable;

public class WorldProviderLibraryOfWorlds extends InstanceWorldProvider
{
    private static final Vec3d FOG_COLOR = new Vec3d(0, 0, 0);


    @Override
    protected void init()
    {
        hasSkyLight = false;
        biomeProvider = BiomeProviders.VOID;
    }

    @Override
    public DimensionType getDimensionType()
    {
        return InstanceTypes.libraryOfWorldsDimType;
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorLibraryOfWorlds(world);
    }

    @Override
    public boolean hasSkyLight()
    {
        return false;
    }

    @Override
    public boolean canRespawnHere()
    {
        return false;
    }

    @Nullable
    @Override
    public IRenderHandler getSkyRenderer()
    {
        return BlankRenderer.BLANK_RENDERER;
    }

    @Nullable
    @Override
    public IRenderHandler getCloudRenderer()
    {
        return BlankRenderer.BLANK_RENDERER;
    }

    @Override
    public Vec3d getFogColor(float p_76562_1_, float p_76562_2_)
    {
        return FOG_COLOR;
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
