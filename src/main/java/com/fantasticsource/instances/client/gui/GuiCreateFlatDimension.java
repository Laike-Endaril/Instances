package com.fantasticsource.instances.client.gui;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiCreateFlatDimension extends GuiScreen
{
    private final GuiCreateDimension createDimensionGui;
    private FlatGeneratorInfo theFlatGeneratorInfo = FlatGeneratorInfo.getDefaultFlatGenerator();
    /**
     * The title given to the flat world currently in creation
     */
    private String flatWorldTitle;
    /**
     * The text used to identify the material for a layer
     */
    private String materialText;
    /**
     * The text used to identify the height of a layer
     */
    private String heightText;
    private GuiCreateFlatDimension.Details createFlatWorldListSlotGui;
    /**
     * The (unused and permenantly hidden) add layer button
     */
    private GuiButton addLayerButton;
    /**
     * The (unused and permenantly hidden) edit layer button
     */
    private GuiButton editLayerButton;
    /**
     * The remove layer button
     */
    private GuiButton removeLayerButton;

    public GuiCreateFlatDimension(GuiCreateDimension createWorldGuiIn, String preset)
    {
        createDimensionGui = createWorldGuiIn;
        setPreset(preset);
    }

    /**
     * Gets the superflat preset in the text format described on the Superflat article on the Minecraft Wiki
     */
    public String getPreset()
    {
        return theFlatGeneratorInfo.toString();
    }

    /**
     * Sets the superflat preset. Invalid or null values will result in the default superflat preset being used.
     *
     * @param preset The new preset to use in the format described on the Superflat article on the Minecraft Wiki
     */
    public void setPreset(String preset)
    {
        theFlatGeneratorInfo = FlatGeneratorInfo.createFlatGeneratorFromString(preset);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.clear();
        flatWorldTitle = I18n.format("createWorld.customize.flat.title");
        materialText = I18n.format("createWorld.customize.flat.tile");
        heightText = I18n.format("createWorld.customize.flat.height");
        createFlatWorldListSlotGui = new GuiCreateFlatDimension.Details(mc, width, height);
        int halfWidth = width >> 1;
        buttonList.add(addLayerButton = new GuiButton(2, halfWidth - 154, height - 52, 100, 20, I18n.format("createWorld.customize.flat.addLayer") + " (NYI)"));
        buttonList.add(editLayerButton = new GuiButton(3, halfWidth - 50, height - 52, 100, 20, I18n.format("createWorld.customize.flat.editLayer") + " (NYI)"));
        buttonList.add(removeLayerButton = new GuiButton(4, halfWidth - 155, height - 52, 150, 20, I18n.format("createWorld.customize.flat.removeLayer")));
        buttonList.add(new GuiButton(0, halfWidth - 155, height - 28, 150, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(5, halfWidth + 5, height - 52, 150, 20, I18n.format("createWorld.customize.presets")));
        buttonList.add(new GuiButton(1, halfWidth + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
        addLayerButton.visible = editLayerButton.visible = false;
        theFlatGeneratorInfo.updateLayers();
        onLayersChanged();
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        createFlatWorldListSlotGui.handleMouseInput();
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button)
    {
        int i = theFlatGeneratorInfo.getFlatLayers().size() - createFlatWorldListSlotGui.selectedLayer - 1;

        if (button.id == 1)
        {
            mc.displayGuiScreen(createDimensionGui);
        }
        else if (button.id == 0)
        {
            createDimensionGui.chunkProviderSettingsJson = getPreset();
            mc.displayGuiScreen(createDimensionGui);
        }
        else if (button.id == 5)
        {
            mc.displayGuiScreen(new GuiFlatDimensionPresets(this));
        }
        else if (button.id == 4 && hasSelectedLayer())
        {
            theFlatGeneratorInfo.getFlatLayers().remove(i);
            createFlatWorldListSlotGui.selectedLayer = Math.min(createFlatWorldListSlotGui.selectedLayer, theFlatGeneratorInfo.getFlatLayers().size() - 1);
        }

        theFlatGeneratorInfo.updateLayers();
        onLayersChanged();
    }

    /**
     * Would update whether or not the edit and remove buttons are enabled, but is currently disabled and always
     * disables the buttons (which are invisible anyways)
     */
    private void onLayersChanged()
    {
        boolean flag = hasSelectedLayer();
        removeLayerButton.enabled = flag;
        editLayerButton.enabled = flag;
        editLayerButton.enabled = false;
        addLayerButton.enabled = false;
    }

    /**
     * Returns whether there is a valid layer selection
     */
    private boolean hasSelectedLayer()
    {
        return createFlatWorldListSlotGui.selectedLayer > -1 && createFlatWorldListSlotGui.selectedLayer < theFlatGeneratorInfo.getFlatLayers().size();
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
        createFlatWorldListSlotGui.drawScreen(mouseX, mouseY, partialTicks);
        int halfWidth = width >> 1;
        drawCenteredString(fontRenderer, flatWorldTitle, halfWidth, 8, 16777215);
        int i = halfWidth - 108;
        drawString(fontRenderer, materialText, i, 32, 16777215);
        drawString(fontRenderer, heightText, i + 217 - fontRenderer.getStringWidth(heightText), 32, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class Details extends GuiSlot
    {
        /**
         * The currently selected layer; -1 if there is no selection. This is in the order that it is displayed on-
         * screen, with the topmost layer having index 0.
         */
        public int selectedLayer = -1;

        public Details(Minecraft mc, int width, int height)
        {
            super(mc, width, height, 43, height - 60, 24);
        }

        /**
         * Draws an item with a background at the given coordinates. The item and its background are 20 pixels tall/wide
         * (though only the inner 18x18 is actually drawn on)
         *
         * @param x          Display x coordinate; the background is drawn 1 pixel further than this and the itemstack is drawn 2 pixels further
         * @param z          Display z coordinate; the background is drawn 1 pixel further than this and the itemstack is drawn 2 pixels further
         * @param itemToDraw The item to draw
         */
        private void drawItem(int x, int z, ItemStack itemToDraw)
        {
            drawItemBackground(x + 1, z + 1);
            GlStateManager.enableRescaleNormal();

            if (itemToDraw != null)
            {
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemIntoGUI(itemToDraw, x + 2, z + 2);
                RenderHelper.disableStandardItemLighting();
            }

            GlStateManager.disableRescaleNormal();
        }

        /**
         * Draws the background icon for an item, with the indented texture from stats.png
         *
         * @param x Display x coordinate
         * @param y Display y coordinate
         */
        private void drawItemBackground(int x, int y)
        {
            drawItemBackground(x, y, 0, 0);
        }

        /**
         * Draws the background icon for an item, using a texture from stats.png with the given coords
         *
         * @param x        Display x coordinate
         * @param z        Display z coordinate
         * @param textureX Leftmost coordinate of the icon in stats.png to use; only ever set to 0
         * @param textureY Topmost coordinate of the icon in stats.png to use; only ever set to 0
         */
        private void drawItemBackground(int x, int z, int textureX, int textureY)
        {
            GlStateManager.color(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(Gui.STAT_ICONS);

            Tessellator tessellator = Tessellator.getInstance();

            BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(x, z + 18, zLevel).tex(textureX * 0.0078125, (textureY + 18) * 0.0078125).endVertex();
            vertexbuffer.pos(x + 18, z + 18, zLevel).tex((textureX + 18) * 0.0078125F, (textureY + 18) * 0.0078125).endVertex();
            vertexbuffer.pos(x + 18, z, zLevel).tex((textureX + 18) * 0.0078125, textureY * 0.0078125).endVertex();
            vertexbuffer.pos(x, z, zLevel).tex(textureX * 0.0078125, textureY * 0.0078125).endVertex();
            tessellator.draw();
        }

        protected int getSize()
        {
            return theFlatGeneratorInfo.getFlatLayers().size();
        }

        /**
         * The element in the slot that was clicked, boolean for whether it was double clicked or not
         */
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
            selectedLayer = slotIndex;
            onLayersChanged();
        }

        /**
         * Returns true if the element passed in is currently selected
         */
        protected boolean isSelected(int slotIndex)
        {
            return slotIndex == selectedLayer;
        }

        protected void drawBackground()
        {
        }

        protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks)
        {
            FlatLayerInfo flatlayerinfo = theFlatGeneratorInfo.getFlatLayers().get(theFlatGeneratorInfo.getFlatLayers().size() - entryID - 1);
            IBlockState iblockstate = flatlayerinfo.getLayerMaterial();
            Block block = iblockstate.getBlock();
            Item item = Item.getItemFromBlock(block);
            ItemStack itemstack = block != Blocks.AIR ? new ItemStack(item, 1, block.getMetaFromState(iblockstate)) : null;
            String s = itemstack == null ? I18n.format("createWorld.customize.flat.air") : item.getItemStackDisplayName(itemstack);

            drawItem(insideLeft, yPos, itemstack);
            fontRenderer.drawString(s, insideLeft + 23, yPos + 3, 16777215);
            String s1;

            if (entryID == 0)
            {
                s1 = I18n.format("createWorld.customize.flat.layer.top", flatlayerinfo.getLayerCount());
            }
            else if (entryID == theFlatGeneratorInfo.getFlatLayers().size() - 1)
            {
                s1 = I18n.format("createWorld.customize.flat.layer.bottom", flatlayerinfo.getLayerCount());
            }
            else
            {
                s1 = I18n.format("createWorld.customize.flat.layer", flatlayerinfo.getLayerCount());
            }

            fontRenderer.drawString(s1, insideLeft + 215 - fontRenderer.getStringWidth(s1), yPos + 3, 16777215);
        }

        protected int getScrollBarX()
        {
            return width - 70;
        }
    }
}