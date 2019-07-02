package com.fantasticsource.instances.network;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.network.handler.HandlerCreateDimension;
import com.fantasticsource.instances.network.handler.HandlerDimensionSync;
import com.fantasticsource.instances.network.handler.HandlerOpenGui;
import com.fantasticsource.instances.network.messages.MessageCreateDimension;
import com.fantasticsource.instances.network.messages.MessageDimensionSync;
import com.fantasticsource.instances.network.messages.MessageOpenGui;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler
{
    public static SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(Instances.MODID);

    public static void init()
    {
        INSTANCE.registerMessage(HandlerCreateDimension.class, MessageCreateDimension.class, 0, Side.SERVER);
        INSTANCE.registerMessage(HandlerOpenGui.class, MessageOpenGui.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(HandlerDimensionSync.class, MessageDimensionSync.class, 2, Side.CLIENT);
    }
}
