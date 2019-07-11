package com.fantasticsource.instances.client;

import com.fantasticsource.instances.client.gui.GUIScreen;
import com.fantasticsource.instances.client.gui.guielements.VerticalScrollbar;
import com.fantasticsource.instances.client.gui.guielements.rect.*;
import com.fantasticsource.tools.datastructures.Color;

import java.util.ArrayList;

public class PersonalPortalGUI extends GUIScreen
{
    private static final Color
            BLANK = new Color(0),
            BLACK = new Color(0xCC),
            AQUA = new Color(0x3366CC),
            WHITE = new Color(0xFFFFFF33),
            WHITE_2 = new Color(0xFFFFFF77),
            WHITE_3 = new Color(0xFFFFFFAA),
            WHITE_4 = new Color(0xFFFFFFFF),
            TEAL = new Color(0x44FFFFFF);

    public static PersonalPortalGUI personalPortalGUI = new PersonalPortalGUI();
    private static boolean ready = false;

    @Override
    public void initGui()
    {
        if (!ready)
        {
            ready = true;

            //Background
            guiElements.add(new GradientRect(0, 0, 1, 1, BLACK, BLACK, AQUA, AQUA));

            //Left
            ArrayList<GUIRectElement> subElements = new ArrayList<>();
            //TODO enable this and see how it goes beyond the boundary of its container...need to clip that somehow
//            subElements.add(new GradientRect(0, 0, 0.5, 1.5, new Color(0xFFFFFFFF), new Color(0xFF), new Color(0xFFFFFFFF), new Color(0xFF)));
//            subElements.add(new GradientRect(0.1, 0, 0.3, 1.5, new Color(0xFFFFFFFF), new Color(0xFF), new Color(0xFFFFFFFF), new Color(0xFF)));
            int i = 0;
            for (double y = 0.01; y < 1.5; y += 0.1)
            {
                subElements.add(new GUITextRect(1d / 60, y, 17d / 60, "Test " + i++, WHITE_3, TEAL, WHITE_4));
            }

            GUIRectElement element = new GradientBorder(0, 0, 19d / 60, 1, 1d / 15, WHITE, BLANK);
            GUIRectScrollView scrollView = new GUIRectScrollView(element, width, height, subElements.toArray(new GUIRectElement[0]));
            guiElements.add(scrollView);
            guiElements.add(new VerticalScrollbar(19d / 60, 0, 1d / 3, 1, WHITE_2, BLANK, WHITE_2, BLANK, scrollView));

            //Separator
            guiElements.add(new GradientRect(1d / 3, 0, 41d / 120, 1, WHITE_2, WHITE_3, WHITE_3, WHITE_2));

            //Center
            guiElements.add(new GradientBorder(41d / 120, 0, 2d / 3, 1d / 10, 1d / 50, WHITE_2, BLANK));
            element = new GradientBorder(41d / 120, 1d / 10, 39d / 60, 1, 1d / 15, WHITE, BLANK);
            scrollView = new GUIRectScrollView(element, width, height);
            guiElements.add(scrollView);
            guiElements.add(new VerticalScrollbar(39d / 60, 1d / 10, 2d / 3, 1, WHITE_2, BLANK, WHITE_2, BLANK, scrollView));

            //Separator
            guiElements.add(new GradientRect(2d / 3, 0, 81d / 120, 1, WHITE_2, WHITE_3, WHITE_3, WHITE_2));

            //Right
            element = new GradientBorder(81d / 120, 0, 59d / 60, 1, 1d / 15, WHITE, BLANK);
            scrollView = new GUIRectScrollView(element, width, height);
            guiElements.add(scrollView);
            guiElements.add(new VerticalScrollbar(59d / 60, 0, 1, 1, WHITE_2, BLANK, WHITE_2, BLANK, scrollView));
        }
    }
}
