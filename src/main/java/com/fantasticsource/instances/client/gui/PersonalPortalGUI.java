package com.fantasticsource.instances.client.gui;

import com.fantasticsource.instances.network.Network;
import com.fantasticsource.instances.network.Network.PersonalPortalGUIPacket;
import com.fantasticsource.mctools.gui.GUILeftClickEvent;
import com.fantasticsource.mctools.gui.GUIScreen;
import com.fantasticsource.mctools.gui.element.GUIElement;
import com.fantasticsource.mctools.gui.element.other.GUIGradient;
import com.fantasticsource.mctools.gui.element.other.GUIGradientBorder;
import com.fantasticsource.mctools.gui.element.other.GUIVerticalScrollbar;
import com.fantasticsource.mctools.gui.element.text.GUIText;
import com.fantasticsource.mctools.gui.element.view.GUIScrollView;
import com.fantasticsource.tools.datastructures.Color;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PersonalPortalGUI extends GUIScreen
{
    private static final Color
            BLANK = new Color(0),
            BLACK = new Color(0xEE),
            BLUE = new Color(0x0055DD),
            WHITE = new Color(0xFFFFFF33),
            WHITE_2 = new Color(0xFFFFFF77),
            WHITE_3 = new Color(0xFFFFFFFF),
            TEAL = new Color(0x338F8FFF),
            TEAL_2 = new Color(0x44FFFFFF);

    private static final PersonalPortalGUI GUI = new PersonalPortalGUI();
    private static GUIScrollView scrollView;

    static
    {
        MinecraftForge.EVENT_BUS.register(PersonalPortalGUI.class);


    }

    public static void show(PersonalPortalGUIPacket packet)
    {
        Minecraft.getMinecraft().displayGuiScreen(GUI);

        scrollView.clear();

        if (packet.isInInstance)
        {
            scrollView.add(new GUIText(GUI, "\n"));
            scrollView.add(new GUIText(GUI, "  Leave Instance\n", TEAL, TEAL_2, WHITE_3));
        }

        if (!packet.isInOwnedInstance)
        {
            scrollView.add(new GUIText(GUI, "\n"));
            scrollView.add(new GUIText(GUI, "  Go Home\n", TEAL, TEAL_2, WHITE_3));
        }

        for (String name : packet.namesOut)
        {
            scrollView.add(new GUIText(GUI, "\n"));
            scrollView.add(new GUIText(GUI, "  Visit " + name + "\n", TEAL, TEAL_2, WHITE_3));
        }

        scrollView.add(new GUIText(GUI, "\n"));
    }

    @Override
    protected void init()
    {
        //Background
        guiElements.add(new GUIGradient(this, 0, 0, 1, 1, BLACK, BLACK, BLUE, BLUE));

        guiElements.add(new GUIGradientBorder(this, 0, 0, 0.98, 1, 1d / 32, WHITE, BLANK));

        scrollView = new GUIScrollView(this, 0, 0, 0.98, 1);
        guiElements.add(scrollView);
        guiElements.add(new GUIVerticalScrollbar(this, 0.98, 0, 0.02, 1, WHITE_2, BLANK, WHITE_2, BLANK, scrollView));
    }

    @SubscribeEvent
    public static void mouseClick(GUILeftClickEvent event)
    {
        if (event.getScreen() == GUI)
        {
            GUIElement element = event.getElement();
            if (element instanceof GUIText)
            {
                String name = element.toString().trim().replace("Visit ", "");
                if (!name.equals(""))
                {
                    GUI.close();
                    Network.WRAPPER.sendToServer(new Network.PersonalPortalPacket(name));
                }
            }
        }
    }
}
