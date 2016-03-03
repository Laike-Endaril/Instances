package lumien.simpledimensions.server;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public class WorldCustom extends WorldServer
{
    private WorldServer delegate;
    private IBorderListener borderListener;
    private static final String __OBFID = "CL_00001430";

    public WorldCustom(WorldInfo worldInfo,MinecraftServer server, ISaveHandler saveHandlerIn, int dimensionId, WorldServer delegate, Profiler profilerIn)
    {
        super(server, saveHandlerIn, worldInfo, dimensionId, profilerIn);
        this.delegate = delegate;
        this.borderListener = new IBorderListener()
        {
            private static final String __OBFID = "CL_00002273";
            @Override
			public void onSizeChanged(WorldBorder border, double newSize)
            {
                WorldCustom.this.getWorldBorder().setTransition(newSize);
            }
            @Override
			public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time)
            {
                WorldCustom.this.getWorldBorder().setTransition(oldSize, newSize, time);
            }
            @Override
			public void onCenterChanged(WorldBorder border, double x, double z)
            {
                WorldCustom.this.getWorldBorder().setCenter(x, z);
            }
            @Override
			public void onWarningTimeChanged(WorldBorder border, int newTime)
            {
                WorldCustom.this.getWorldBorder().setWarningTime(newTime);
            }
            @Override
			public void onWarningDistanceChanged(WorldBorder border, int newDistance)
            {
                WorldCustom.this.getWorldBorder().setWarningDistance(newDistance);
            }
            @Override
			public void onDamageAmountChanged(WorldBorder border, double newAmount)
            {
                WorldCustom.this.getWorldBorder().setDamageAmount(newAmount);
            }
            @Override
			public void onDamageBufferChanged(WorldBorder border, double newSize)
            {
                WorldCustom.this.getWorldBorder().setDamageBuffer(newSize);
            }
        };
        this.delegate.getWorldBorder().addListener(this.borderListener);
    }

    /**
     * Saves the chunks to disk.
     */
    @Override
	protected void saveLevel() throws MinecraftException
    {
        this.perWorldStorage.saveAllData();
    }

    @Override
	public World init()
    {
        this.mapStorage = this.delegate.getMapStorage();
        this.worldScoreboard = this.delegate.getScoreboard();
        String s = VillageCollection.fileNameForProvider(this.provider);
        VillageCollection villagecollection = (VillageCollection)this.perWorldStorage.loadData(VillageCollection.class, s);

        if (villagecollection == null)
        {
            this.villageCollectionObj = new VillageCollection(this);
            this.perWorldStorage.setData(s, this.villageCollectionObj);
        }
        else
        {
            this.villageCollectionObj = villagecollection;
            this.villageCollectionObj.setWorldsForAll(this);
        }

        return this;
    }


    /**
     * Syncs all changes to disk and wait for completion.
     */
    @Override
    public void flush()
    {
        super.flush();
        this.delegate.getWorldBorder().removeListener(this.borderListener); // Unlink ourselves, to prevent world leak.
    }
}