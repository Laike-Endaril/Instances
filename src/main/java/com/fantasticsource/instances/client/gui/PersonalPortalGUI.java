package com.fantasticsource.instances.client.gui;

import com.fantasticsource.instances.client.gui.guielements.VerticalScrollbar;
import com.fantasticsource.instances.client.gui.guielements.rect.*;
import com.fantasticsource.tools.datastructures.Color;

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


    @Override
    public void initGui()
    {
        super.initGui();

        //Background
        guiElements.add(new GradientRect(0, 0, 1, 1, BLACK, BLACK, AQUA, AQUA));

        //Single scrollview for now
        ArrayList<GUIRectElement> subElements = new ArrayList<>();

        double y = V_PADDING;
        subElements.add(new GUITextRect(H_PADDING, y, SEPARATION_POINT - H_PADDING, "Go Home", TEAL, TEAL_2, WHITE_3));
        for (String name : names)
        {
            y += 0.05;
            subElements.add(new GUITextRect(H_PADDING, y, SEPARATION_POINT - H_PADDING, "Visit " + name, TEAL, TEAL_2, WHITE_3));
        }
        for (int i = 0; i < 20; i++)
        {
            y += 0.05;
            subElements.add(new GUITextRect(H_PADDING, y, SEPARATION_POINT - H_PADDING, "Test " + i, TEAL, TEAL_2, WHITE_3));
        }
        subElements.add(new GradientRect(0, 0, SEPARATION_POINT, y + V_PADDING, BLANK, BLANK, BLANK, BLANK));

        GUIRectElement element = new GradientBorder(0, 0, SEPARATION_POINT, 1, 1d / 15, WHITE, BLANK);
        GUIRectScrollView scrollView = new GUIRectScrollView(element, width, height, subElements.toArray(new GUIRectElement[0]));
        guiElements.add(scrollView);
        guiElements.add(new VerticalScrollbar(SEPARATION_POINT, 0, 1, 1, WHITE_2, BLANK, WHITE_2, BLANK, scrollView));
    }
}
