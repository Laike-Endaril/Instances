package com.fantasticsource.instances.client.gui;

import com.fantasticsource.instances.network.PacketHandler;
import com.fantasticsource.instances.network.messages.MessageCreateDimension;
import com.fantasticsource.instances.util.WorldInfoSimple;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class GuiCreateDimension extends GuiScreen
{
    /**
     * These filenames are known to be restricted on one or more OS's.
     */
    private static final String[] disallowedFilenames = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
    public String chunkProviderSettingsJson = "";
    private GuiTextField dimensionNameTextField;
    private GuiTextField seedTextField;
    private String saveDirName;
    private String gameType = "survival";
    private String savedGameMode;
    private boolean generateStructures = true;
    private boolean allowCheats;
    private boolean alreadyGenerated;
    private boolean userInMoreOptions;
    private GuiButton btnMoreOptions;
    private GuiButton btnStructures;
    private GuiButton btnDimensionType;
    private GuiButton btnCustomizeType;
    private GuiButton btnEnvironmentType;
    private String worldSeed;
    private String worldName;
    private int selectedIndex;
    private int selectedEnvironmentIndex;

    public GuiCreateDimension()
    {
        worldSeed = "";
        worldName = I18n.format("instances.newDimension");
    }

    private static String getDisplayableName(String input)
    {
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.replace("_", " ").toCharArray())
        {
            if (Character.isSpaceChar(c))
            {
                nextTitleCase = true;
            }
            else if (nextTitleCase)
            {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }

    public static String getUncollidingSaveDirName(ISaveFormat saveLoader, String name)
    {
        name = name.replaceAll("[\\./\"]", "_");

        StringBuilder nameBuilder = new StringBuilder(name);
        for (String s1 : disallowedFilenames)
        {
            if (nameBuilder.toString().equalsIgnoreCase(s1))
            {
                nameBuilder = new StringBuilder("_" + nameBuilder + "_");
            }
        }
        name = nameBuilder.toString();

        while (saveLoader.getWorldInfo(name) != null)
        {
            name = name + "-";
        }

        return name;
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen()
    {
        dimensionNameTextField.updateCursorCounter();
        seedTextField.updateCursorCounter();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        int halfWidth = width >> 1;
        buttonList.add(new GuiButton(0, halfWidth - 155, height - 28, 150, 20, I18n.format("instances.create")));
        buttonList.add(new GuiButton(1, halfWidth + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
        buttonList.add(btnMoreOptions = new GuiButton(3, halfWidth - 75, 187, 150, 20, I18n.format("instances.moreDimensionOptions")));
        buttonList.add(btnStructures = new GuiButton(4, halfWidth - 155, 100, 150, 20, I18n.format("selectWorld.mapFeatures")));
        btnStructures.visible = false;
        buttonList.add(btnDimensionType = new GuiButton(5, halfWidth + 5, 100, 150, 20, I18n.format("selectWorld.mapType")));
        btnDimensionType.visible = false;
        buttonList.add(btnCustomizeType = new GuiButton(8, halfWidth + 5, 120, 150, 20, I18n.format("selectWorld.customizeType")));
        btnCustomizeType.visible = false;
        buttonList.add(btnEnvironmentType = new GuiButton(11, halfWidth - 155, 151, 150, 20, I18n.format("instances.environmentType")));
        btnEnvironmentType.visible = false;
        dimensionNameTextField = new GuiTextField(9, fontRenderer, halfWidth - 100, 60, 200, 20);
        dimensionNameTextField.setFocused(true);
        dimensionNameTextField.setText(worldName);
        seedTextField = new GuiTextField(10, fontRenderer, halfWidth - 100, 60, 200, 20);
        seedTextField.setText(worldSeed);
        showMoreWorldOptions(userInMoreOptions);
        calcSaveDirName();
        updateDisplayState();
    }

    private void calcSaveDirName()
    {
        saveDirName = dimensionNameTextField.getText().trim();
        char[] achar = ChatAllowedCharacters.ILLEGAL_FILE_CHARACTERS;
        int i = achar.length;

        for (int j = 0; j < i; ++j)
        {
            char c0 = achar[j];
            saveDirName = saveDirName.replace(c0, '_');
        }

        if (StringUtils.isEmpty(saveDirName))
        {
            saveDirName = "World";
        }

        saveDirName = getUncollidingSaveDirName(mc.getSaveLoader(), saveDirName);
    }

    private void updateDisplayState()
    {
        btnStructures.displayString = I18n.format("selectWorld.mapFeatures") + " ";

        if (generateStructures)
        {
            btnStructures.displayString = btnStructures.displayString + I18n.format("options.on");
        }
        else
        {
            btnStructures.displayString = btnStructures.displayString + I18n.format("options.off");
        }

        btnDimensionType.displayString = I18n.format("selectWorld.mapType") + " " + I18n.format(WorldType.WORLD_TYPES[selectedIndex].getTranslationKey());
        btnEnvironmentType.displayString = I18n.format("instances.environmentType") + " " + I18n.format(getDisplayableName(DimensionType.values()[selectedEnvironmentIndex].getName()));
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat
     * events
     */
    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == 1)
            {
                mc.displayGuiScreen(null); // (parentScreen);
            }
            else if (button.id == 0)
            {
                mc.displayGuiScreen(null);

                if (alreadyGenerated)
                {
                    return;
                }

                alreadyGenerated = true;
                long i = (new Random()).nextLong();
                String s = seedTextField.getText();

                if (!StringUtils.isEmpty(s))
                {
                    try
                    {
                        long j = Long.parseLong(s);

                        if (j != 0L)
                        {
                            i = j;
                        }
                    }
                    catch (NumberFormatException numberformatexception)
                    {
                        i = s.hashCode();
                    }
                }

                WorldType.WORLD_TYPES[selectedIndex].onGUICreateWorldPress();

                GameType gametype = GameType.getByName(gameType);
                WorldSettings worldsettings = new WorldSettings(i, gametype, generateStructures, false, WorldType.WORLD_TYPES[selectedIndex]);
                worldsettings.setGeneratorOptions(chunkProviderSettingsJson);

                if (allowCheats) worldsettings.enableCommands();

                WorldInfoSimple worldInfo = new WorldInfoSimple(worldsettings, dimensionNameTextField.getText().trim(), DimensionType.values()[selectedEnvironmentIndex]);
                MessageCreateDimension createMessage = new MessageCreateDimension(worldInfo);

                PacketHandler.INSTANCE.sendToServer(createMessage);
            }
            else if (button.id == 3)
            {
                toggleMoreWorldOptions();
            }
            else if (button.id == 4)
            {
                generateStructures = !generateStructures;
                updateDisplayState();
            }
            else if (button.id == 5)
            {
                ++selectedIndex;

                if (selectedIndex >= WorldType.WORLD_TYPES.length)
                {
                    selectedIndex = 0;
                }

                while (!canSelectCurWorldType())
                {
                    ++selectedIndex;

                    if (selectedIndex >= WorldType.WORLD_TYPES.length)
                    {
                        selectedIndex = 0;
                    }
                }

                chunkProviderSettingsJson = "";
                updateDisplayState();
                showMoreWorldOptions(userInMoreOptions);
            }
            else if (button.id == 11)
            {
                ++selectedEnvironmentIndex;

                if (selectedEnvironmentIndex >= DimensionType.values().length)
                {
                    selectedEnvironmentIndex = 0;
                }

                updateDisplayState();
                showMoreWorldOptions(userInMoreOptions);
            }
            else if (button.id == 8)
            {
                if (WorldType.WORLD_TYPES[selectedIndex] == WorldType.FLAT)
                {
                    mc.displayGuiScreen(new GuiCreateFlatDimension(this, chunkProviderSettingsJson));
                }
                else if (WorldType.WORLD_TYPES[selectedIndex] == WorldType.CUSTOMIZED)
                {
                    mc.displayGuiScreen(new GuiCustomizeDimension(this, chunkProviderSettingsJson));
                }
            }
        }
    }

    private boolean canSelectCurWorldType()
    {
        WorldType worldtype = WorldType.WORLD_TYPES[selectedIndex];
        return worldtype != null && worldtype.canBeCreated() && (worldtype != WorldType.DEBUG_ALL_BLOCK_STATES || isShiftKeyDown());
    }

    private void toggleMoreWorldOptions()
    {
        showMoreWorldOptions(!userInMoreOptions);
    }

    private void showMoreWorldOptions(boolean toggle)
    {
        userInMoreOptions = toggle;

        if (WorldType.WORLD_TYPES[selectedIndex] == WorldType.DEBUG_ALL_BLOCK_STATES)
        {
            if (savedGameMode == null)
            {
                savedGameMode = gameType;
            }

            gameType = "spectator";
            btnStructures.visible = false;
            btnDimensionType.visible = userInMoreOptions;
            btnCustomizeType.visible = false;
            btnEnvironmentType.visible = false;
        }
        else
        {
            if (savedGameMode != null)
            {
                gameType = savedGameMode;
                savedGameMode = null;
            }

            btnStructures.visible = userInMoreOptions && WorldType.WORLD_TYPES[selectedIndex] != WorldType.CUSTOMIZED;
            btnDimensionType.visible = userInMoreOptions;
            btnCustomizeType.visible = userInMoreOptions && WorldType.WORLD_TYPES[selectedIndex].isCustomizable();
            btnEnvironmentType.visible = userInMoreOptions;
        }

        updateDisplayState();

        if (userInMoreOptions)
        {
            btnMoreOptions.displayString = I18n.format("gui.done");
        }
        else
        {
            btnMoreOptions.displayString = I18n.format("instances.moreDimensionOptions");
        }
    }

    /**
     * Fired when a key is typed (except F11 who toggle full screen). This is
     * the equivalent of KeyListener.keyTyped(KeyEvent e). Args : character
     * (character on the key), keyCode (lwjgl Keyboard key code)
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        if (dimensionNameTextField.isFocused() && !userInMoreOptions)
        {
            dimensionNameTextField.textboxKeyTyped(typedChar, keyCode);
            worldName = dimensionNameTextField.getText();
        }
        else if (seedTextField.isFocused() && userInMoreOptions)
        {
            seedTextField.textboxKeyTyped(typedChar, keyCode);
            worldSeed = seedTextField.getText();
        }

        if (keyCode == 28 || keyCode == 156)
        {
            actionPerformed(buttonList.get(0));
        }

        buttonList.get(0).enabled = dimensionNameTextField.getText().length() > 0;
        calcSaveDirName();
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (userInMoreOptions)
        {
            seedTextField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else
        {
            dimensionNameTextField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY,
     * renderPartialTicks
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        int halfWidth = width >> 1;
        drawCenteredString(fontRenderer, I18n.format("instances.create"), halfWidth, 20, -1);

        if (userInMoreOptions)
        {
            drawString(fontRenderer, I18n.format("selectWorld.enterSeed"), halfWidth - 100, 47, -6250336);
            drawString(fontRenderer, I18n.format("selectWorld.seedInfo"), halfWidth - 100, 85, -6250336);

            if (btnStructures.visible)
            {
                drawString(fontRenderer, I18n.format("selectWorld.mapFeatures.info"), halfWidth - 150, 122, -6250336);
            }

            if (btnEnvironmentType.visible)
            {
                drawString(fontRenderer, I18n.format("instances.environmentType.info"), halfWidth - 150, 172, -6250336);
            }

            seedTextField.drawTextBox();

            if (WorldType.WORLD_TYPES[selectedIndex].hasInfoNotice())
            {
                fontRenderer.drawSplitString(I18n.format(WorldType.WORLD_TYPES[selectedIndex].getInfoTranslationKey()), btnDimensionType.x + 2, btnDimensionType.y + 22, btnDimensionType.getButtonWidth(), 10526880);
            }
        }
        else
        {
            drawString(fontRenderer, I18n.format("instances.enterName"), halfWidth - 100, 47, -6250336);
            drawString(fontRenderer, I18n.format("selectWorld.resultFolder") + " " + saveDirName, halfWidth - 100, 85, -6250336);
            dimensionNameTextField.drawTextBox();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void recreateFromExistingWorld(WorldInfo original)
    {
        worldName = I18n.format("selectWorld.newWorld.copyOf", original.getWorldName());
        worldSeed = original.getSeed() + "";
        selectedIndex = original.getTerrainType().getId();
        chunkProviderSettingsJson = original.getGeneratorOptions();
        generateStructures = original.isMapFeaturesEnabled();
        allowCheats = original.areCommandsAllowed();

        if (original.isHardcoreModeEnabled()) gameType = "hardcore";
        else if (original.getGameType().isSurvivalOrAdventure()) gameType = "survival";
        else if (original.getGameType().isCreative()) gameType = "creative";
    }
}