package com.fantasticsource.instances.client.gui;

import com.fantasticsource.instances.client.gui.guielements.GUIElement;
import com.fantasticsource.instances.client.gui.guielements.VerticalScrollbar;
import com.fantasticsource.instances.client.gui.guielements.rect.*;
import com.fantasticsource.tools.datastructures.Color;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

public class PersonalPortalGUI extends GUIScreen
{
    private static final double
            SEPARATION_POINT = 58d / 60,
            H_PADDING = 1d / 60,
            V_PADDING = 0.01;

    private static final Color
            BLANK = new Color(0),
            BLACK = new Color(0xCC),
            AQUA = new Color(0x3366CC),
            WHITE = new Color(0xFFFFFF33),
            WHITE_2 = new Color(0xFFFFFF77),
            WHITE_3 = new Color(0xFFFFFFFF),
            TEAL = new Color(0x227F7FFF),
            TEAL_2 = new Color(0x44FFFFFF);

    public static PersonalPortalGUI personalPortalGUI = new PersonalPortalGUI();
    public static String[] names;

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
            if (element instanceof GUITextRect)
            {
                Minecraft.getMinecraft().player.closeScreen();
                switch (element.toString())
                {
                    case "Go Home":
                        System.out.println("Home");
                        break;
                    default:
                        System.out.println("Visit");
                }
            }
        }
    }

    @Override
    public void initGui()
    {
        super.initGui();

        //Background
        guiElements.add(new GradientRect(this, 0, 0, 1, 1, BLACK, BLACK, AQUA, AQUA));

        //Single scrollview for now
        ArrayList<GUIRectElement> subElements = new ArrayList<>();

        double y = V_PADDING;
        subElements.add(new GUITextRect(this, H_PADDING, y, SEPARATION_POINT - H_PADDING, "Go Home", TEAL, TEAL_2, WHITE_3));
        for (String name : names)
        {
            y += 0.05;
            subElements.add(new GUITextRect(this, H_PADDING, y, SEPARATION_POINT - H_PADDING, "Visit " + name, TEAL, TEAL_2, WHITE_3));
        }
        subElements.add(new GradientRect(this, 0, 0, SEPARATION_POINT, y + V_PADDING, BLANK, BLANK, BLANK, BLANK));

        GUIRectElement element = new GradientBorder(this, 0, 0, SEPARATION_POINT, 1, 1d / 15, WHITE, BLANK);
        GUIRectScrollView scrollView = new GUIRectScrollView(this, element, width, height, subElements.toArray(new GUIRectElement[0]));
        guiElements.add(scrollView);
        guiElements.add(new VerticalScrollbar(this, SEPARATION_POINT, 0, 1, 1, WHITE_2, BLANK, WHITE_2, BLANK, scrollView));
    }
}
