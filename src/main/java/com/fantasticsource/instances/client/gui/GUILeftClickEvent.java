package com.fantasticsource.instances.client.gui;

import com.fantasticsource.instances.client.gui.guielements.GUIElement;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class GUILeftClickEvent extends Event
{
    private GUIScreen screen;
    private GUIElement element;

    public GUILeftClickEvent(GUIScreen screen, GUIElement element)
    {
        this.screen = screen;
        this.element = element;
    }

    public GUIScreen getScreen()
    {
        return screen;
    }

    public GUIElement getElement()
    {
        return element;
    }
}
