package com.fantasticsource.instances.tags;

import com.fantasticsource.fantasticlib.api.FLibAPI;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.UUID;

import static com.fantasticsource.fantasticlib.FantasticLib.MODID;

public class SkyroomVisitors
{
    public static void init()
    {
        FLibAPI.attachNBTCapToWorldIf(MODID, world -> world.provider.getDimension() == 0);
    }


    public static boolean setVisitable(MinecraftServer server, UUID visitor, UUID visitable, boolean canVisit)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(server.worlds[0]).getCompound(MODID);

        if (!compound.hasKey("visitableToVisitors"))
        {
            if (canVisit)
            {
                compound.setTag("visitorToVisitables", new NBTTagCompound());
                compound.setTag("visitableToVisitors", new NBTTagCompound());
            }
            else return false;
        }

        NBTTagCompound compound2 = compound.getCompoundTag("visitableToVisitors");
        compound = compound.getCompoundTag("visitorToVisitables");

        if (!compound.hasKey("" + visitor))
        {
            if (canVisit)
            {
                compound.setTag("" + visitor, new NBTTagList());
                if (!compound2.hasKey("" + visitable)) compound2.setTag("" + visitable, new NBTTagList());
            }
            else return false;
        }

        NBTTagList list = compound.getTagList("" + visitor, Constants.NBT.TAG_STRING), list2 = compound2.getTagList("" + visitable, Constants.NBT.TAG_STRING);

        boolean actionTaken = false;
        for (int i = 0; i < list.tagCount(); i++)
        {
            if (list.getStringTagAt(i).equals("" + visitable))
            {
                list.removeTag(i);
                actionTaken = true;
                break;
            }
        }

        for (int i = 0; i < list2.tagCount(); i++)
        {
            if (list.getStringTagAt(i).equals("" + visitor))
            {
                list.removeTag(i);
                actionTaken = true;
                break;
            }
        }

        if (!canVisit) return actionTaken;


        list.appendTag(new NBTTagString("" + visitable));
        list2.appendTag(new NBTTagString("" + visitor));
        return !actionTaken; //This is reversed in this case; if true, then we removed the thing before re-adding it
    }


    public static ArrayList<String> visitables(MinecraftServer server, UUID visitor)
    {
        ArrayList<String> result = new ArrayList<>();

        NBTTagCompound compound = FLibAPI.getNBTCap(server.worlds[0]).getCompound(MODID);

        if (!compound.hasKey("visitorToVisitables")) return result;
        compound = compound.getCompoundTag("visitorToVisitables");

        if (!compound.hasKey("" + visitor)) return result;
        NBTTagList list = compound.getTagList("" + visitor, Constants.NBT.TAG_STRING);

        for (int i = 0; i < list.tagCount(); i++)
        {
            result.add(list.getStringTagAt(i));
        }
        return result;
    }

    public static boolean canVisit(MinecraftServer server, UUID visitor, UUID visitable)
    {
        return visitables(server, visitor).contains("" + visitable);
    }


    public static ArrayList<String> visitors(MinecraftServer server, UUID visitable)
    {
        ArrayList<String> result = new ArrayList<>();

        NBTTagCompound compound = FLibAPI.getNBTCap(server.worlds[0]).getCompound(MODID);

        if (!compound.hasKey("visitableToVisitors")) return result;
        compound = compound.getCompoundTag("visitableToVisitors");

        if (!compound.hasKey("" + visitable)) return result;
        NBTTagList list = compound.getTagList("" + visitable, Constants.NBT.TAG_STRING);

        for (int i = 0; i < list.tagCount(); i++)
        {
            result.add(list.getStringTagAt(i));
        }
        return result;
    }

    public static boolean canBeVisitedBy(MinecraftServer server, UUID visitable, UUID visitor)
    {
        return visitors(server, visitable).contains("" + visitor);
    }
}
