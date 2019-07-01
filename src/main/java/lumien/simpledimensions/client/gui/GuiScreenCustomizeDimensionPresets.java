package lumien.simpledimensions.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiScreenCustomizeDimensionPresets extends GuiScreen
{
    private static final List<GuiScreenCustomizeDimensionPresets.Info> field_175310_f = new ArrayList<>();

    static
    {
        ChunkGeneratorSettings.Factory ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{ \"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":5000.0, \"mainNoiseScaleY\":1000.0, \"mainNoiseScaleZ\":5000.0, \"baseSize\":8.5, \"stretchY\":8.0, \"biomeDepthWeight\":2.0, \"biomeDepthOffset\":0.5, \"biomeScaleWeight\":2.0, \"biomeScaleOffset\":0.375, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":255 }");
        ResourceLocation resourcelocation = new ResourceLocation("textures/gui/presets/water.png");
        field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(I18n.format("createWorld.customize.custom.preset.waterWorld"), resourcelocation, ChunkGeneratorSettings$factory));

        ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":3000.0, \"heightScale\":6000.0, \"upperLimitScale\":250.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":10.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
        resourcelocation = new ResourceLocation("textures/gui/presets/isles.png");
        field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(I18n.format("createWorld.customize.custom.preset.isleLand"), resourcelocation, ChunkGeneratorSettings$factory));

        ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":5000.0, \"mainNoiseScaleY\":1000.0, \"mainNoiseScaleZ\":5000.0, \"baseSize\":8.5, \"stretchY\":5.0, \"biomeDepthWeight\":2.0, \"biomeDepthOffset\":1.0, \"biomeScaleWeight\":4.0, \"biomeScaleOffset\":1.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
        resourcelocation = new ResourceLocation("textures/gui/presets/delight.png");
        field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(I18n.format("createWorld.customize.custom.preset.caveDelight"), resourcelocation, ChunkGeneratorSettings$factory));

        ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":738.41864, \"heightScale\":157.69133, \"upperLimitScale\":801.4267, \"lowerLimitScale\":1254.1643, \"depthNoiseScaleX\":374.93652, \"depthNoiseScaleZ\":288.65228, \"depthNoiseScaleExponent\":1.2092624, \"mainNoiseScaleX\":1355.9908, \"mainNoiseScaleY\":745.5343, \"mainNoiseScaleZ\":1183.464, \"baseSize\":1.8758626, \"stretchY\":1.7137525, \"biomeDepthWeight\":1.7553768, \"biomeDepthOffset\":3.4701107, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":2.535211, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
        resourcelocation = new ResourceLocation("textures/gui/presets/madness.png");
        field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(I18n.format("createWorld.customize.custom.preset.mountains"), resourcelocation, ChunkGeneratorSettings$factory));

        ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":1000.0, \"mainNoiseScaleY\":3000.0, \"mainNoiseScaleZ\":1000.0, \"baseSize\":8.5, \"stretchY\":10.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":20 }");
        resourcelocation = new ResourceLocation("textures/gui/presets/drought.png");
        field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(I18n.format("createWorld.customize.custom.preset.drought"), resourcelocation, ChunkGeneratorSettings$factory));

        ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":2.0, \"lowerLimitScale\":64.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":12.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":6 }");
        resourcelocation = new ResourceLocation("textures/gui/presets/chaos.png");
        field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(I18n.format("createWorld.customize.custom.preset.caveChaos"), resourcelocation, ChunkGeneratorSettings$factory));

        ChunkGeneratorSettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":12.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":true, \"seaLevel\":40 }");
        resourcelocation = new ResourceLocation("textures/gui/presets/luck.png");
        field_175310_f.add(new GuiScreenCustomizeDimensionPresets.Info(I18n.format("createWorld.customize.custom.preset.goodLuck"), resourcelocation, ChunkGeneratorSettings$factory));
    }

    private String field_175315_a = "Customize World Presets";
    private GuiScreenCustomizeDimensionPresets.ListPreset field_175311_g;
    private GuiButton field_175316_h;
    private GuiTextField field_175317_i;
    private GuiCustomizeDimension customizeDimensionGui;
    private String field_175313_s;
    private String field_175312_t;

    GuiScreenCustomizeDimensionPresets(GuiCustomizeDimension customizeDimensionGui)
    {
        this.customizeDimensionGui = customizeDimensionGui;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        field_175315_a = I18n.format("createWorld.customize.custom.presets.title");
        field_175313_s = I18n.format("createWorld.customize.presets.share");
        field_175312_t = I18n.format("createWorld.customize.presets.list");
        field_175317_i = new GuiTextField(2, fontRenderer, 50, 40, width - 100, 20);
        field_175311_g = new GuiScreenCustomizeDimensionPresets.ListPreset();
        field_175317_i.setMaxStringLength(2000);
        field_175317_i.setText(customizeDimensionGui.func_175323_a());
        buttonList.add(field_175316_h = new GuiButton(0, width / 2 - 102, height - 27, 100, 20, I18n.format("createWorld.customize.presets.select")));
        buttonList.add(new GuiButton(1, width / 2 + 3, height - 27, 100, 20, I18n.format("gui.cancel")));
        func_175304_a();
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        field_175311_g.handleMouseInput();
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
        field_175317_i.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (!field_175317_i.textboxKeyTyped(typedChar, keyCode))
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 0:
                customizeDimensionGui.func_175324_a(field_175317_i.getText());
                mc.displayGuiScreen(customizeDimensionGui);
                break;
            case 1:
                mc.displayGuiScreen(customizeDimensionGui);
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
        field_175311_g.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRenderer, field_175315_a, width / 2, 8, 16777215);
        drawString(fontRenderer, field_175313_s, 50, 30, 10526880);
        drawString(fontRenderer, field_175312_t, 50, 70, 10526880);
        field_175317_i.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        field_175317_i.updateCursorCounter();
        super.updateScreen();
    }

    private void func_175304_a()
    {
        field_175316_h.enabled = func_175305_g();
    }

    private boolean func_175305_g()
    {
        return field_175311_g.field_178053_u > -1 && field_175311_g.field_178053_u < field_175310_f.size() || field_175317_i.getText().length() > 1;
    }

    static class Info
    {
        String field_178955_a;
        ResourceLocation field_178953_b;
        ChunkGeneratorSettings.Factory field_178954_c;

        Info(String p_i45523_1_, ResourceLocation p_i45523_2_, ChunkGeneratorSettings.Factory p_i45523_3_)
        {
            field_178955_a = p_i45523_1_;
            field_178953_b = p_i45523_2_;
            field_178954_c = p_i45523_3_;
        }
    }

    class ListPreset extends GuiSlot
    {
        int field_178053_u = -1;

        public ListPreset()
        {
            super(GuiScreenCustomizeDimensionPresets.this.mc, GuiScreenCustomizeDimensionPresets.this.width, GuiScreenCustomizeDimensionPresets.this.height, 80, GuiScreenCustomizeDimensionPresets.this.height - 32, 38);
        }

        protected int getSize()
        {
            return GuiScreenCustomizeDimensionPresets.field_175310_f.size();
        }

        /**
         * The element in the slot that was clicked, boolean for whether it was double clicked or not
         */
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
            this.field_178053_u = slotIndex;
            GuiScreenCustomizeDimensionPresets.this.func_175304_a();
            GuiScreenCustomizeDimensionPresets.this.field_175317_i.setText(GuiScreenCustomizeDimensionPresets.field_175310_f.get(GuiScreenCustomizeDimensionPresets.this.field_175311_g.field_178053_u).field_178954_c.toString());
        }

        /**
         * Returns true if the element passed in is currently selected
         */
        protected boolean isSelected(int slotIndex)
        {
            return slotIndex == this.field_178053_u;
        }

        protected void drawBackground()
        {
        }

        private void func_178051_a(int p_178051_1_, int p_178051_2_, ResourceLocation p_178051_3_)
        {
            int i = p_178051_1_ + 5;
            GuiScreenCustomizeDimensionPresets.this.drawHorizontalLine(i - 1, i + 32, p_178051_2_ - 1, -2039584);
            GuiScreenCustomizeDimensionPresets.this.drawHorizontalLine(i - 1, i + 32, p_178051_2_ + 32, -6250336);
            GuiScreenCustomizeDimensionPresets.this.drawVerticalLine(i - 1, p_178051_2_ - 1, p_178051_2_ + 32, -2039584);
            GuiScreenCustomizeDimensionPresets.this.drawVerticalLine(i + 32, p_178051_2_ - 1, p_178051_2_ + 32, -6250336);
            GlStateManager.color(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(p_178051_3_);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(i, p_178051_2_ + 32, 0).tex(0, 1).endVertex();
            vertexbuffer.pos((i + 32), p_178051_2_ + 32, 0).tex(1, 1).endVertex();
            vertexbuffer.pos((i + 32), p_178051_2_, 0).tex(1, 0).endVertex();
            vertexbuffer.pos(i, p_178051_2_, 0).tex(0, 0).endVertex();
            tessellator.draw();
        }

        protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks)
        {
            GuiScreenCustomizeDimensionPresets.Info guiscreencustomizepresets$info = GuiScreenCustomizeDimensionPresets.field_175310_f.get(entryID);
            func_178051_a(insideLeft, yPos, guiscreencustomizepresets$info.field_178953_b);
            GuiScreenCustomizeDimensionPresets.this.fontRenderer.drawString(guiscreencustomizepresets$info.field_178955_a, insideLeft + 42, yPos + 14, 16777215);
        }
    }
}