package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.boimes.BiomeProviders;
import com.fantasticsource.instances.world.boimes.BiomeVoid;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.tools.Tools;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;

public class WorldProviderLibraryOfWorlds extends WorldProvider
{
    private static final String TYPE_NAME = InstanceTypes.skyroomDimType.getName().replaceAll(" ", "_");
    private final boolean XAEROS;
    private static final Vec3d FOG_COLOR = new Vec3d(0, 0, 0);

    public WorldProviderLibraryOfWorlds()
    {
        XAEROS = Loader.isModLoaded("xaeroworldmap");
    }


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

    @Nullable
    @Override
    public String getSaveFolder()
    {
        if (XAEROS && world.isRemote && Tools.stackContainsSubstring("xaero")) return "instancesInstance";

        int dim = getDimension();
        InstanceWorldInfo info = InstanceHandler.get(dim);
        if (info == null) return null;

        UUID owner = info.getOwner();

        return "instances" + File.separator + TYPE_NAME + File.separator + (owner != null ? owner : info.getWorldName());
    }
}
