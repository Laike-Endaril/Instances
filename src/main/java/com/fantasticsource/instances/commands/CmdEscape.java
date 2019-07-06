package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;

import java.util.Set;

public class CmdEscape extends CommandBase
{

    public static void escape(Entity entity)
    {
        DimensionType type = entity.world.provider.getDimensionType();
        if (type != InstanceTypes.skyroomDimType && type != InstanceTypes.libraryOfWorldsDimType) return;

        Set<String> strings = entity.getTags();
        for (String s : strings.toArray(new String[0]))
        {
            if (s.contains("instances.lastgoodpos"))
            {
                String[] tokens = s.replace("instances.lastgoodpos", "").split(",");
                CmdTPD.tpd(entity, Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]), entity.rotationYaw, entity.rotationPitch);
                break;
            }
        }
    }

    @Override
    public String getName()
    {
        return "escapeinstance";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "escapeinstance";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (sender instanceof Entity)
        {
            Entity entity = (Entity) sender;
            DimensionType type = entity.world.provider.getDimensionType();
            if (type != InstanceTypes.skyroomDimType && type != InstanceTypes.libraryOfWorldsDimType)
            {
                sender.sendMessage(new TextComponentString("This command can only be used when in a skyroom or Library of Worlds instance"));
                return;
            }

            escape(entity);
        }
    }
}
