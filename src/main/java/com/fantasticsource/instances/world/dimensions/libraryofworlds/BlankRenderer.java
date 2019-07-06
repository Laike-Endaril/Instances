package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.client.IRenderHandler;

public class BlankRenderer extends IRenderHandler
{
    public static final IRenderHandler BLANK_RENDERER = new BlankRenderer();

    private BlankRenderer()
    {
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc)
    {
    }
}
