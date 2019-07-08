package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.server.Teleport;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;

public class CmdEscape extends CommandBase
{
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

            Teleport.escape(entity);
        }
    }
}
