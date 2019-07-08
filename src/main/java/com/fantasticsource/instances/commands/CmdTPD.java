package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

public class CmdTPD extends CommandBase
{
    public String getName()
    {
        return "tpd";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getUsage(ICommandSender sender)
    {
        return Instances.MODID + ".commands.tpd.usage";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        Teleport.teleport(this, server, sender, null, args);
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            ArrayList<String> strings = new ArrayList<>();

            for (InstanceWorldInfo info : InstanceHandler.instanceInfo.values()) strings.add(info.getWorldName());
            for (int id : DimensionManager.getIDs()) strings.add("" + id);

            return getListOfStringsMatchingLastWord(args, strings);
        }
        return new ArrayList<>();
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0 || index == 1;
    }
}
