package com.fantasticsource.instances.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

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
        //TODO
    }
}
