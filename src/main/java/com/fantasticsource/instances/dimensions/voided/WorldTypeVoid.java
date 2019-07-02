package com.fantasticsource.instances.dimensions.voided;

import com.fantasticsource.instances.Instances;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class WorldTypeVoid extends WorldType
{
    public static int voidDimID;
    public static DimensionType voidDimType;
    public static String voidDimName = "Void Instance";

    private WorldTypeVoid()
    {
        super(voidDimName);
    }

    public static void init()
    {
        //Indirectly initializes via super constructor
        new WorldTypeVoid();

        int i;
        for (i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++)
        {
            if (!DimensionManager.isDimensionRegistered(i))
            {
                break;
            }
        }

        if (i == Integer.MAX_VALUE) throw new IllegalStateException("All dimensions used; this should absolutely never happen");
        voidDimID = i;

        voidDimType = DimensionType.register(voidDimName, "_" + voidDimName, voidDimID, WorldProviderVoid.class, false);
        DimensionManager.registerDimension(i, voidDimType);
    }

    @SideOnly(Side.CLIENT)
    public String getTranslationKey()
    {
        return Instances.MODID + ".worldType.void";
    }

    @Override
    public double getHorizon(World world)
    {
        return 0;
    }

    @Override
    public BiomeProvider getBiomeProvider(World world)
    {
        return new BiomeProviderSingle(BiomeVoid.voidBiome);
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions)
    {
        return new ChunkGeneratorVoid(world);
    }

    @Override
    public int getMinimumSpawnHeight(World world)
    {
        return world.getSeaLevel() + 1;
    }

    @Override
    public double voidFadeMagnitude()
    {
        return Double.MAX_VALUE;
    }

    @Override
    public boolean handleSlimeSpawnReduction(Random random, World world)
    {
        return true;
    }

    @Override
    public int getSpawnFuzz(WorldServer world, net.minecraft.server.MinecraftServer server)
    {
        return 0;
    }
}