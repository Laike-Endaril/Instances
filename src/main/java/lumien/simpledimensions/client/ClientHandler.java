package lumien.simpledimensions.client;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ClientHandler
{
    static ClientHandler INSTANCE;

    HashSet<Integer> simpleDimensions;

    public ClientHandler()
    {
        simpleDimensions = new HashSet<Integer>();
    }

    public static ClientHandler getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new ClientHandler();
        }

        return INSTANCE;
    }

    public void cleanUp()
    {
        for (Integer i : simpleDimensions)
        {
            if (DimensionManager.isDimensionRegistered(i))
            {
                DimensionManager.unregisterDimension(i);
            }
        }
    }

    public void sync(HashMap<Integer, DimensionType> dimensions)
    {
        this.cleanUp();

        this.simpleDimensions = new HashSet<Integer>();
        this.simpleDimensions.addAll(dimensions.keySet());

        for (Map.Entry<Integer, DimensionType> entry : dimensions.entrySet())
        {
            if (!DimensionManager.isDimensionRegistered(entry.getKey()))
            {
                DimensionManager.registerDimension(entry.getKey(), entry.getValue());
            }
        }
    }
}
