package com.fantasticsource.instances.client.gui;

import com.google.common.collect.Maps;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiFlatDimensionPresets extends GuiScreen
{
    private static final List<GuiFlatDimensionPresets.LayerItem> FLAT_WORLD_PRESETS = new ArrayList<>();

    static
    {
        registerPreset("Classic Flat", Item.getItemFromBlock(Blocks.GRASS), Biomes.PLAINS, Collections.singletonList("village"), new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(2, Blocks.DIRT), new FlatLayerInfo(1, Blocks.BEDROCK));
        registerPreset("Tunnelers\' Dream", Item.getItemFromBlock(Blocks.STONE), Biomes.EXTREME_HILLS, Arrays.asList("biome_1", "dungeon", "decoration", "stronghold", "mineshaft"), new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(5, Blocks.DIRT), new FlatLayerInfo(230, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        registerPreset("Water World", Items.WATER_BUCKET, Biomes.DEEP_OCEAN, Arrays.asList("biome_1", "oceanmonument"), new FlatLayerInfo(90, Blocks.WATER), new FlatLayerInfo(5, Blocks.SAND), new FlatLayerInfo(5, Blocks.DIRT), new FlatLayerInfo(5, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        registerPreset("Overworld", Item.getItemFromBlock(Blocks.TALLGRASS), BlockTallGrass.EnumType.GRASS.getMeta(), Biomes.PLAINS, Arrays.asList("village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon", "lake", "lava_lake"), new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(59, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        registerPreset("Snowy Kingdom", Item.getItemFromBlock(Blocks.SNOW_LAYER), Biomes.ICE_PLAINS, Arrays.asList("village", "biome_1"), new FlatLayerInfo(1, Blocks.SNOW_LAYER), new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(59, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        registerPreset("Bottomless Pit", Items.FEATHER, Biomes.PLAINS, Arrays.asList("village", "biome_1"), new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(2, Blocks.COBBLESTONE));
        registerPreset("Desert", Item.getItemFromBlock(Blocks.SAND), Biomes.DESERT, Arrays.asList("village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon"), new FlatLayerInfo(8, Blocks.SAND), new FlatLayerInfo(52, Blocks.SANDSTONE), new FlatLayerInfo(3, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        registerPreset("Redstone Ready", Items.REDSTONE, Biomes.DESERT, new FlatLayerInfo(52, Blocks.SANDSTONE), new FlatLayerInfo(3, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        registerPreset("The Void", Item.getItemFromBlock(Blocks.BARRIER), Biomes.VOID, Collections.singletonList("decoration"), new FlatLayerInfo(1, Blocks.AIR));
    }

    /**
     * The parent GUI
     */
    private final GuiCreateFlatDimension parentScreen;
    private String presetsTitle;
    private String presetsShare;
    private String field_146436_r;
    private GuiFlatDimensionPresets.ListSlot field_146435_s;
    private GuiButton field_146434_t;
    private GuiTextField field_146433_u;

    GuiFlatDimensionPresets(GuiCreateFlatDimension p_i46318_1_)
    {
        this.parentScreen = p_i46318_1_;
    }

    private static void registerPreset(String name, Item icon, Biome biome, FlatLayerInfo... layers)
    {
        registerPreset(name, icon, 0, biome, null, layers);
    }

    private static void registerPreset(String name, Item icon, Biome biome, List<String> features, FlatLayerInfo... layers)
    {
        registerPreset(name, icon, 0, biome, features, layers);
    }

    private static void registerPreset(String name, Item icon, int iconMetadata, Biome biome, List<String> features, FlatLayerInfo... layers)
    {
        FlatGeneratorInfo flatgeneratorinfo = new FlatGeneratorInfo();

        for (int i = layers.length - 1; i >= 0; --i)
        {
            flatgeneratorinfo.getFlatLayers().add(layers[i]);
        }

        flatgeneratorinfo.setBiome(Biome.getIdForBiome(biome));
        flatgeneratorinfo.updateLayers();

        if (features != null)
        {
            for (String s : features)
            {
                flatgeneratorinfo.getWorldFeatures().put(s, Maps.newHashMap());
            }
        }

        FLAT_WORLD_PRESETS.add(new GuiFlatDimensionPresets.LayerItem(icon, iconMetadata, name, flatgeneratorinfo.toString()));
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        this.presetsTitle = I18n.format("createWorld.customize.presets.title");
        this.presetsShare = I18n.format("createWorld.customize.presets.share");
        this.field_146436_r = I18n.format("createWorld.customize.presets.list");
        this.field_146433_u = new GuiTextField(2, this.fontRenderer, 50, 40, this.width - 100, 20);
        this.field_146435_s = new GuiFlatDimensionPresets.ListSlot();
        this.field_146433_u.setMaxStringLength(1230);
        this.field_146433_u.setText(this.parentScreen.getPreset());
        this.buttonList.add(this.field_146434_t = new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("createWorld.customize.presets.select")));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));
        this.func_146426_g();
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.field_146435_s.handleMouseInput();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        this.field_146433_u.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (!this.field_146433_u.textboxKeyTyped(typedChar, keyCode))
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == 0 && this.func_146430_p())
        {
            this.parentScreen.setPreset(this.field_146433_u.getText());
            this.mc.displayGuiScreen(this.parentScreen);
        }
        else if (button.id == 1)
        {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    /**
     * Draws the screen and all the components in it.
     *
     * @param mouseX       Mouse x coordinate
     * @param mouseY       Mouse y coordinate
     * @param partialTicks How far into the current tick (1/20th of a second) the game is
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        field_146435_s.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRenderer, presetsTitle, width >> 1, 8, 16777215);
        drawString(fontRenderer, presetsShare, 50, 30, 10526880);
        drawString(fontRenderer, field_146436_r, 50, 70, 10526880);
        field_146433_u.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        this.field_146433_u.updateCursorCounter();
        super.updateScreen();
    }

    private void func_146426_g()
    {
        field_146434_t.enabled = func_146430_p();
    }

    private boolean func_146430_p()
    {
        return this.field_146435_s.field_148175_k > -1 && this.field_146435_s.field_148175_k < FLAT_WORLD_PRESETS.size() || this.field_146433_u.getText().length() > 1;
    }

    static class LayerItem
    {
        public String name;
        Item icon;
        int iconMetadata;
        String generatorInfo;

        LayerItem(Item iconIn, int iconMetadataIn, String nameIn, String generatorInfoIn)
        {
            this.icon = iconIn;
            this.iconMetadata = iconMetadataIn;
            this.name = nameIn;
            this.generatorInfo = generatorInfoIn;
        }
    }

    class ListSlot extends GuiSlot
    {
        int field_148175_k = -1;

        ListSlot()
        {
            super(GuiFlatDimensionPresets.this.mc, GuiFlatDimensionPresets.this.width, GuiFlatDimensionPresets.this.height, 80, GuiFlatDimensionPresets.this.height - 37, 24);
        }

        private void renderIcon(int p_178054_1_, int p_178054_2_, Item icon, int iconMetadata)
        {
            this.func_148173_e(p_178054_1_ + 1, p_178054_2_ + 1);
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
            GuiFlatDimensionPresets.this.itemRender.renderItemIntoGUI(new ItemStack(icon, 1, iconMetadata), p_178054_1_ + 2, p_178054_2_ + 2);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
        }

        private void func_148173_e(int p_148173_1_, int p_148173_2_)
        {
            this.func_148171_c(p_148173_1_, p_148173_2_);
        }

        private void func_148171_c(int p_148171_1_, int p_148171_2_)
        {
            GlStateManager.color(1, 1, 1, 1);
            this.mc.getTextureManager().bindTexture(Gui.STAT_ICONS);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(p_148171_1_, p_148171_2_ + 18, GuiFlatDimensionPresets.this.zLevel).tex(0 * 0.0078125, 0.140625).endVertex();
            vertexbuffer.pos(p_148171_1_ + 18, p_148171_2_ + 18, GuiFlatDimensionPresets.this.zLevel).tex(0.140625, 0.140625).endVertex();
            vertexbuffer.pos(p_148171_1_ + 18, p_148171_2_, GuiFlatDimensionPresets.this.zLevel).tex(0.140625, 0).endVertex();
            vertexbuffer.pos(p_148171_1_, p_148171_2_, GuiFlatDimensionPresets.this.zLevel).tex(0, 0).endVertex();
            tessellator.draw();
        }

        protected int getSize()
        {
            return GuiFlatDimensionPresets.FLAT_WORLD_PRESETS.size();
        }

        /**
         * The element in the slot that was clicked, boolean for whether it was double clicked or not
         */
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
            this.field_148175_k = slotIndex;
            GuiFlatDimensionPresets.this.func_146426_g();
            GuiFlatDimensionPresets.this.field_146433_u.setText(GuiFlatDimensionPresets.FLAT_WORLD_PRESETS.get(GuiFlatDimensionPresets.this.field_146435_s.field_148175_k).generatorInfo);
        }

        /**
         * Returns true if the element passed in is currently selected
         */
        protected boolean isSelected(int slotIndex)
        {
            return slotIndex == this.field_148175_k;
        }

        protected void drawBackground()
        {
        }

        protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks)
        {
            GuiFlatDimensionPresets.LayerItem guiflatpresets$layeritem = GuiFlatDimensionPresets.FLAT_WORLD_PRESETS.get(entryID);
            this.renderIcon(insideLeft, yPos, guiflatpresets$layeritem.icon, guiflatpresets$layeritem.iconMetadata);
            GuiFlatDimensionPresets.this.fontRenderer.drawString(guiflatpresets$layeritem.name, insideLeft + 18 + 5, yPos + 6, 16777215);
        }
    }
}