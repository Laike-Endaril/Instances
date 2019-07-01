package lumien.simpledimensions.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class TeleporterSimple extends Teleporter
{

    public TeleporterSimple(WorldServer worldIn)
    {
        super(worldIn);
    }

    @Override
    public void placeInPortal(Entity entityIn, float rotationYaw)
    {
    }

    @Override
    public boolean placeInExistingPortal(Entity entityIn, float p_180620_2_)
    {
        return true;
    }

    @Override
    public boolean makePortal(Entity p_85188_1_)
    {
        return true;
    }
}
