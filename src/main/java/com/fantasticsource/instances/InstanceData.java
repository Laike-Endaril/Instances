package com.fantasticsource.instances;

import com.fantasticsource.instances.tags.savefile.Owners;
import com.fantasticsource.instances.tags.savefile.Visitors;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.tools.Tools;
import net.minecraft.entity.Entity;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.util.UUID;
import java.util.regex.Matcher;

public class InstanceData
{
    protected String fullName, shortName;
    protected DimensionType dimensionType;
    protected boolean saves;

    protected InstanceData(String fullName, boolean saves, DimensionType dimensionType, String shortName)
    {
        this.fullName = fullName;
        this.dimensionType = dimensionType;
        this.saves = saves;
        this.shortName = shortName;
    }

    public static InstanceData get(boolean saves, DimensionType instanceType, String shortName)
    {
        return get((saves ? "Saved" : "Temporary") + File.separator + instanceType.getName() + File.separator + shortName);
    }

    public static InstanceData get(Entity entity)
    {
        return get(entity.world);
    }

    public static InstanceData get(World world)
    {
        return get(world.provider);
    }

    public static InstanceData get(WorldProvider worldProvider)
    {
        return get(MCTools.getSaveFolder(worldProvider).replace("Instances" + File.separator, ""));
    }

    public static InstanceData get(String fullName)
    {
        fullName = Tools.fixFileSeparators(fullName);
        String[] tokens = Tools.fixedSplit(fullName, Matcher.quoteReplacement(File.separator));

        //Validate minimum argument count
        if (tokens.length < 3) return null;

        //Validate save mode
        boolean saves = tokens[0].equals("Saved");
        if (!saves && !tokens[0].equals("Temporary")) return null;

        //Validate dimension type
        DimensionType dimensionType = MCTools.getDimensionType(tokens[1]);
        if (dimensionType == null) return null;

        //Validate path
        //TODO maybe improve path validation at some point
        for (int i = 2; i < tokens.length; i++)
        {
            if (tokens[i].contains(" ") || tokens[i].trim().equals("") || tokens[i].contains("..")) return null;
        }

        return new InstanceData(fullName, saves, dimensionType, tokens[tokens.length - 1]);
    }


    public String getFullName()
    {
        return fullName;
    }

    public String getShortName()
    {
        return shortName;
    }

    public DimensionType getDimensionType()
    {
        return dimensionType;
    }

    public boolean saves()
    {
        return saves;
    }

    public boolean exists()
    {
        return new File(InstanceHandler.getInstancesDir(FMLCommonHandler.instance().getMinecraftServerInstance()) + fullName).isDirectory();
    }

    public UUID getOwner()
    {
        return Owners.getOwner(fullName);
    }

    public UUID[] validVisitors()
    {
        return Visitors.validVisitors(fullName);
    }

    public boolean canVisit(UUID playerID)
    {
        return Visitors.canVisit(playerID, fullName);
    }
}
