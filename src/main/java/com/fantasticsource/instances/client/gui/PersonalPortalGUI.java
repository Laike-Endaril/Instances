package com.fantasticsource.instances.client.gui;

import com.fantasticsource.instances.network.Network;
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
    private static final double
            SEPARATION_POINT = 58d / 60,
            H_PADDING = 1d / 60,
            V_PADDING = 0.06;

    private static final Color
            BLANK = new Color(0),
            BLACK = new Color(0xEE),
            BLUE = new Color(0x0055DD),
            WHITE = new Color(0xFFFFFF33),
            WHITE_2 = new Color(0xFFFFFF77),
            WHITE_3 = new Color(0xFFFFFFFF),
            TEAL = new Color(0x338F8FFF),
            TEAL_2 = new Color(0x44FFFFFF);

    public static PersonalPortalGUI personalPortalGUI = new PersonalPortalGUI();
    public static String[] names;
    public static boolean isInInstance, isInOwnedInstance;

    static
    {
        MinecraftForge.EVENT_BUS.register(PersonalPortalGUI.class);
    }

    @SubscribeEvent
    public static void mouseClick(GUILeftClickEvent event)
    {
        if (event.getScreen() == personalPortalGUI)
        {
            GUIElement element = event.getElement();
            if (element instanceof GUIText)
            {
                Minecraft.getMinecraft().player.closeScreen();
                Network.WRAPPER.sendToServer(new Network.PersonalPortalPacket(element.toString().replace("Visit ", "")));
            }
        }
    }

    @Override
    protected void init()
    {
        //Background
        guiElements.add(new GUIGradient(this, 0, 0, 1, 1, BLACK, BLACK, BLUE, BLUE));

        guiElements.add(new GUIGradientBorder(this, 0, 0, SEPARATION_POINT, 1, 1d / 32, WHITE, BLANK));

        GUIScrollView scrollView = new GUIScrollView(this, 0, 0, SEPARATION_POINT, 1);
        guiElements.add(scrollView);
        guiElements.add(new GUIVerticalScrollbar(this, SEPARATION_POINT, 0, 1 - SEPARATION_POINT, 1, WHITE_2, BLANK, WHITE_2, BLANK, scrollView));

        double y = -V_PADDING / 2;
        if (isInInstance)
        {
            y += V_PADDING;
            guiElements.add(new GUIText(this, H_PADDING, y, "Leave Instance", TEAL, TEAL_2, WHITE_3));
        }

        if (!isInOwnedInstance)
        {
            y += V_PADDING;
            guiElements.add(new GUIText(this, H_PADDING, y, "Go Home", TEAL, TEAL_2, WHITE_3));
        }

        for (String name : names)
        {
            y += V_PADDING;
            guiElements.add(new GUIText(this, H_PADDING, y, "Visit " + name, TEAL, TEAL_2, WHITE_3));
        }

        guiElements.add(new GUIGradient(this, 0, 0, SEPARATION_POINT, y + V_PADDING, BLANK, BLANK, BLANK, BLANK));
    }
}
