package com.fantasticsource.instances.instancetypes.skyroom;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.instancetypes.voided.BiomeVoid;
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

public class WorldTypeSkyroom extends WorldType
{
    public static int skyroomDimID;
    public static DimensionType skyroomDimType;
    public static String skyroomDimName = "Skyroom Instance";

    private WorldTypeSkyroom()
    {
        super(skyroomDimName);
    }

    public static void init()
    {
        //Indirectly initializes via super constructor
        new WorldTypeSkyroom();

        int i;
        for (i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++)
        {
            if (!DimensionManager.isDimensionRegistered(i))
            {
                break;
            }
        }

        if (i == Integer.MAX_VALUE) throw new IllegalStateException("All dimensions used; this should absolutely never happen");
        skyroomDimID = i;

        skyroomDimType = DimensionType.register(skyroomDimName, "_" + skyroomDimName, skyroomDimID, WorldProviderSkyroom.class, false);
        DimensionManager.registerDimension(i, skyroomDimType);
    }

    @SideOnly(Side.CLIENT)
    public String getTranslationKey()
    {
        return Instances.MODID + ".worldType.skyroom";
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
        return new ChunkGeneratorSkyroom(world);
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
