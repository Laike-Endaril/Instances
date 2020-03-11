package com.fantasticsource.instances.tags.entity;

import com.fantasticsource.fantasticlib.api.FLibAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static com.fantasticsource.fantasticlib.FantasticLib.MODID;

public class CurrentWorldname
{
    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        if (!(player instanceof EntityPlayerMP)) return;

        setCurrentWorldName((EntityPlayerMP) player);
    }

    public static void setCurrentWorldName(EntityPlayerMP player)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(player).getCompound(MODID);

        compound.setString("worldName", player.world.getWorldInfo().getWorldName());
    }

    public static String getCurrentWorldName(EntityPlayerMP player)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(player).getCompound(MODID);

        if (!compound.hasKey("worldName")) return null;

        return compound.getString("worldName");
    }
}
