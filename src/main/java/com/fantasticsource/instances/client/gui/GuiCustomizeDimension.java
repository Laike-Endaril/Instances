package com.fantasticsource.instances.client.gui;

import com.fantasticsource.instances.Instances;
import com.google.common.base.Predicate;
import com.google.common.primitives.Floats;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class GuiCustomizeDimension extends GuiScreen implements GuiSlider.FormatHelper, GuiPageButtonList.GuiResponder
{
    private String field_175341_a = "Customize Dimension Settings";
    private String field_175333_f = "Page 1 of 3";
    private String field_175335_g = "Basic Settings";
    private String[] field_175342_h = new String[4];
    private GuiCreateDimension guiCreateDimension;
    private GuiPageButtonList guiPageButtonList;
    private GuiButton guiButton;
    private GuiButton guiButton1;
    private GuiButton guiButton2;
    private GuiButton guiButton3;
    private GuiButton guiButton4;
    private GuiButton guiButtonYes;
    private GuiButton guiButtonNo;
    private GuiButton guiButton7;
    private boolean field_175338_A = false;
    private int field_175339_B = 0;
    private boolean field_175340_C = false;

    private Predicate<String> isValidFloatStr = o ->
    {
        if (o == null) return false;

        float f = Float.parseFloat(o);
        return o.length() == 0 || Floats.isFinite(f) && f >= 0;
    };

    private ChunkGeneratorSettings.Factory field_175334_E = new ChunkGeneratorSettings.Factory();
    private ChunkGeneratorSettings.Factory field_175336_F;
    /**
     * A Random instance for this world customization
     */
    private Random random = new Random();

    public GuiCustomizeDimension(GuiScreen p_i45521_1_, String p_i45521_2_)
    {
        guiCreateDimension = (GuiCreateDimension) p_i45521_1_;
        func_175324_a(p_i45521_2_);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        field_175341_a = I18n.format(Instances.MODID + ".customizeTitle");
        buttonList.clear();
        buttonList.add(guiButton3 = new GuiButton(302, 20, 5, 80, 20, I18n.format("createWorld.customize.custom.prev")));
        buttonList.add(guiButton4 = new GuiButton(303, width - 100, 5, 80, 20, I18n.format("createWorld.customize.custom.next")));
        buttonList.add(guiButton2 = new GuiButton(304, width / 2 - 187, height - 27, 90, 20, I18n.format("createWorld.customize.custom.defaults")));
        buttonList.add(guiButton1 = new GuiButton(301, width / 2 - 92, height - 27, 90, 20, I18n.format("createWorld.customize.custom.randomize")));
        buttonList.add(guiButton7 = new GuiButton(305, width / 2 + 3, height - 27, 90, 20, I18n.format("createWorld.customize.custom.presets")));
        buttonList.add(guiButton = new GuiButton(300, width / 2 + 98, height - 27, 90, 20, I18n.format("gui.done")));
        guiButtonYes = new GuiButton(306, width / 2 - 55, 160, 50, 20, I18n.format("gui.yes"));
        guiButtonYes.visible = false;
        buttonList.add(guiButtonYes);
        guiButtonNo = new GuiButton(307, width / 2 + 5, 160, 50, 20, I18n.format("gui.no"));
        guiButtonNo.visible = false;
        buttonList.add(guiButtonNo);
        func_175325_f();
    }

    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        guiPageButtonList.handleMouseInput();
    }

    private void func_175325_f()
    {
        GuiPageButtonList.GuiListEntry[] aguilistentry = new GuiPageButtonList.GuiListEntry[]{new GuiPageButtonList.GuiSlideEntry(160, I18n.format("createWorld.customize.custom.seaLevel"), true, this, 1, 255, field_175336_F.seaLevel), new GuiPageButtonList.GuiButtonEntry(148, I18n.format("createWorld.customize.custom.useCaves"), true, field_175336_F.useCaves), new GuiPageButtonList.GuiButtonEntry(150, I18n.format("createWorld.customize.custom.useStrongholds"), true, field_175336_F.useStrongholds), new GuiPageButtonList.GuiButtonEntry(151, I18n.format("createWorld.customize.custom.useVillages"), true, field_175336_F.useVillages), new GuiPageButtonList.GuiButtonEntry(152, I18n.format("createWorld.customize.custom.useMineShafts"), true, field_175336_F.useMineShafts), new GuiPageButtonList.GuiButtonEntry(153, I18n.format("createWorld.customize.custom.useTemples"), true, field_175336_F.useTemples), new GuiPageButtonList.GuiButtonEntry(210, I18n.format("createWorld.customize.custom.useMonuments"), true, field_175336_F.useMonuments), new GuiPageButtonList.GuiButtonEntry(154, I18n.format("createWorld.customize.custom.useRavines"), true, field_175336_F.useRavines), new GuiPageButtonList.GuiButtonEntry(149, I18n.format("createWorld.customize.custom.useDungeons"), true, field_175336_F.useDungeons), new GuiPageButtonList.GuiSlideEntry(157, I18n.format("createWorld.customize.custom.dungeonChance"), true, this, 1, 100, field_175336_F.dungeonChance), new GuiPageButtonList.GuiButtonEntry(155, I18n.format("createWorld.customize.custom.useWaterLakes"), true, field_175336_F.useWaterLakes), new GuiPageButtonList.GuiSlideEntry(158, I18n.format("createWorld.customize.custom.waterLakeChance"), true, this, 1, 100, field_175336_F.waterLakeChance), new GuiPageButtonList.GuiButtonEntry(156, I18n.format("createWorld.customize.custom.useLavaLakes"), true, field_175336_F.useLavaLakes), new GuiPageButtonList.GuiSlideEntry(159, I18n.format("createWorld.customize.custom.lavaLakeChance"), true, this, 10, 100, field_175336_F.lavaLakeChance), new GuiPageButtonList.GuiButtonEntry(161, I18n.format("createWorld.customize.custom.useLavaOceans"), true, field_175336_F.useLavaOceans), new GuiPageButtonList.GuiSlideEntry(162, I18n.format("createWorld.customize.custom.fixedBiome"), true, this, -1, 37, field_175336_F.fixedBiome), new GuiPageButtonList.GuiSlideEntry(163, I18n.format("createWorld.customize.custom.biomeSize"), true, this, 1, 8, field_175336_F.biomeSize), new GuiPageButtonList.GuiSlideEntry(164, I18n.format("createWorld.customize.custom.riverSize"), true, this, 1, 5, field_175336_F.riverSize)};
        GuiPageButtonList.GuiListEntry[] aguilistentry1 = new GuiPageButtonList.GuiListEntry[]{new GuiPageButtonList.GuiLabelEntry(416, I18n.format("tile.dirt.name"), false), null, new GuiPageButtonList.GuiSlideEntry(165, I18n.format("createWorld.customize.custom.size"), false, this, 1, 50, field_175336_F.dirtSize), new GuiPageButtonList.GuiSlideEntry(166, I18n.format("createWorld.customize.custom.count"), false, this, 0, 40, field_175336_F.dirtCount), new GuiPageButtonList.GuiSlideEntry(167, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0, 255, field_175336_F.dirtMinHeight), new GuiPageButtonList.GuiSlideEntry(168, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0, 255, field_175336_F.dirtMaxHeight), new GuiPageButtonList.GuiLabelEntry(417, I18n.format("tile.gravel.name"), false), null, new GuiPageButtonList.GuiSlideEntry(169, I18n.format("createWorld.customize.custom.size"), false, this, 1, 50, field_175336_F.gravelSize), new GuiPageButtonList.GuiSlideEntry(170, I18n.format("createWorld.customize.custom.count"), false, this, 0, 40, field_175336_F.gravelCount), new GuiPageButtonList.GuiSlideEntry(171, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0, 255, field_175336_F.gravelMinHeight), new GuiPageButtonList.GuiSlideEntry(172, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0, 255, field_175336_F.gravelMaxHeight), new GuiPageButtonList.GuiLabelEntry(418, I18n.format("tile.stone.granite.name"), false), null, new GuiPageButtonList.GuiSlideEntry(173, I18n.format("createWorld.customize.custom.size"), false, this, 1, 50, field_175336_F.graniteSize), new GuiPageButtonList.GuiSlideEntry(174, I18n.format("createWorld.customize.custom.count"), false, this, 0, 40, field_175336_F.graniteCount), new GuiPageButtonList.GuiSlideEntry(175, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0, 255, field_175336_F.graniteMinHeight), new GuiPageButtonList.GuiSlideEntry(176, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0, 255, field_175336_F.graniteMaxHeight), new GuiPageButtonList.GuiLabelEntry(419, I18n.format("tile.stone.diorite.name"), false), null, new GuiPageButtonList.GuiSlideEntry(177, I18n.format("createWorld.customize.custom.size"), false, this, 1, 50, field_175336_F.dioriteSize), new GuiPageButtonList.GuiSlideEntry(178, I18n.format("createWorld.customize.custom.count"), false, this, 0, 40, field_175336_F.dioriteCount), new GuiPageButtonList.GuiSlideEntry(179, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0, 255, field_175336_F.dioriteMinHeight), new GuiPageButtonList.GuiSlideEntry(180, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0, 255, field_175336_F.dioriteMaxHeight), new GuiPageButtonList.GuiLabelEntry(420, I18n.format("tile.stone.andesite.name"), false), null, new GuiPageButtonList.GuiSlideEntry(181, I18n.format("createWorld.customize.custom.size"), false, this, 1, 50, field_175336_F.andesiteSize), new GuiPageButtonList.GuiSlideEntry(182, I18n.format("createWorld.customize.custom.count"), false, this, 0, 40, field_175336_F.andesiteCount), new GuiPageButtonList.GuiSlideEntry(183, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0, 255, field_175336_F.andesiteMinHeight), new GuiPageButtonList.GuiSlideEntry(184, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0, 255, field_175336_F.andesiteMaxHeight), new GuiPageButtonList.GuiLabelEntry(421, I18n.format("tile.oreCoal.name"), false), null, new GuiPageButtonList.GuiSlideEntry(185, I18n.format("createWorld.customize.custom.size"), false, this, 1, 50, field_175336_F.coalSize), new GuiPageButtonList.GuiSlideEntry(186, I18n.format("createWorld.customize.custom.count"), false, this, 0, 40, field_175336_F.coalCount), new GuiPageButtonList.GuiSlideEntry(187, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0, 255, field_175336_F.coalMinHeight), new GuiPageButtonList.GuiSlideEntry(189, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0, 255, field_175336_F.coalMaxHeight), new GuiPageButtonList.GuiLabelEntry(422, I18n.format("tile.oreIron.name"), false), null, new GuiPageButtonList.GuiSlideEntry(190, I18n.format("createWorld.customize.custom.size"), false, this, 1, 50, field_175336_F.ironSize), new GuiPageButtonList.GuiSlideEntry(191, I18n.format("createWorld.customize.custom.count"), false, this, 0, 40, field_175336_F.ironCount), new GuiPageButtonList.GuiSlideEntry(192, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0, 255, field_175336_F.ironMinHeight), new GuiPageButtonList.GuiSlideEntry(193, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0, 255, field_175336_F.ironMaxHeight), new GuiPageButtonList.GuiLabelEntry(423, I18n.format("tile.oreGold.name"), false), null, new GuiPageButtonList.GuiSlideEntry(194, I18n.format("createWorld.customize.custom.size"), false, this, 1, 50, field_175336_F.goldSize), new GuiPageButtonList.GuiSlideEntry(195, I18n.format("createWorld.customize.custom.count"), false, this, 0, 40, field_175336_F.goldCount), new GuiPageButtonList.GuiSlideEntry(196, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0, 255, field_175336_F.goldMinHeight), new GuiPageButtonList.GuiSlideEntry(197, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0, 255, field_175336_F.goldMaxHeight), new GuiPageButtonList.GuiLabelEntry(424, I18n.format("tile.oreRedstone.name"), false), null, new GuiPageButtonList.GuiSlideEntry(198, I18n.format("createWorld.customize.custom.size"), false, this, 1, 50, field_175336_F.redstoneSize), new GuiPageButtonList.GuiSlideEntry(199, I18n.format("createWorld.customize.custom.count"), false, this, 0, 40, field_175336_F.redstoneCount), new GuiPageButtonList.GuiSlideEntry(200, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0, 255, field_175336_F.redstoneMinHeight), new GuiPageButtonList.GuiSlideEntry(201, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0, 255, field_175336_F.redstoneMaxHeight), new GuiPageButtonList.GuiLabelEntry(425, I18n.format("tile.oreDiamond.name"), false), null, new GuiPageButtonList.GuiSlideEntry(202, I18n.format("createWorld.customize.custom.size"), false, this, 1, 50, field_175336_F.diamondSize), new GuiPageButtonList.GuiSlideEntry(203, I18n.format("createWorld.customize.custom.count"), false, this, 0, 40, field_175336_F.diamondCount), new GuiPageButtonList.GuiSlideEntry(204, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0, 255, field_175336_F.diamondMinHeight), new GuiPageButtonList.GuiSlideEntry(205, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0, 255, field_175336_F.diamondMaxHeight), new GuiPageButtonList.GuiLabelEntry(426, I18n.format("tile.oreLapis.name"), false), null, new GuiPageButtonList.GuiSlideEntry(206, I18n.format("createWorld.customize.custom.size"), false, this, 1, 50, field_175336_F.lapisSize), new GuiPageButtonList.GuiSlideEntry(207, I18n.format("createWorld.customize.custom.count"), false, this, 0, 40, field_175336_F.lapisCount), new GuiPageButtonList.GuiSlideEntry(208, I18n.format("createWorld.customize.custom.center"), false, this, 0, 255, field_175336_F.lapisCenterHeight), new GuiPageButtonList.GuiSlideEntry(209, I18n.format("createWorld.customize.custom.spread"), false, this, 0, 255, field_175336_F.lapisSpread)};
        GuiPageButtonList.GuiListEntry[] aguilistentry2 = new GuiPageButtonList.GuiListEntry[]{new GuiPageButtonList.GuiSlideEntry(100, I18n.format("createWorld.customize.custom.mainNoiseScaleX"), false, this, 1, 5000, field_175336_F.mainNoiseScaleX), new GuiPageButtonList.GuiSlideEntry(101, I18n.format("createWorld.customize.custom.mainNoiseScaleY"), false, this, 1, 5000, field_175336_F.mainNoiseScaleY), new GuiPageButtonList.GuiSlideEntry(102, I18n.format("createWorld.customize.custom.mainNoiseScaleZ"), false, this, 1, 5000, field_175336_F.mainNoiseScaleZ), new GuiPageButtonList.GuiSlideEntry(103, I18n.format("createWorld.customize.custom.depthNoiseScaleX"), false, this, 1, 2000, field_175336_F.depthNoiseScaleX), new GuiPageButtonList.GuiSlideEntry(104, I18n.format("createWorld.customize.custom.depthNoiseScaleZ"), false, this, 1, 2000, field_175336_F.depthNoiseScaleZ), new GuiPageButtonList.GuiSlideEntry(105, I18n.format("createWorld.customize.custom.depthNoiseScaleExponent"), false, this, 0.01F, 20, field_175336_F.depthNoiseScaleExponent), new GuiPageButtonList.GuiSlideEntry(106, I18n.format("createWorld.customize.custom.baseSize"), false, this, 1, 25, field_175336_F.baseSize), new GuiPageButtonList.GuiSlideEntry(107, I18n.format("createWorld.customize.custom.coordinateScale"), false, this, 1, 6000, field_175336_F.coordinateScale), new GuiPageButtonList.GuiSlideEntry(108, I18n.format("createWorld.customize.custom.heightScale"), false, this, 1, 6000, field_175336_F.heightScale), new GuiPageButtonList.GuiSlideEntry(109, I18n.format("createWorld.customize.custom.stretchY"), false, this, 0.01F, 50, field_175336_F.stretchY), new GuiPageButtonList.GuiSlideEntry(110, I18n.format("createWorld.customize.custom.upperLimitScale"), false, this, 1, 5000, field_175336_F.upperLimitScale), new GuiPageButtonList.GuiSlideEntry(111, I18n.format("createWorld.customize.custom.lowerLimitScale"), false, this, 1, 5000, field_175336_F.lowerLimitScale), new GuiPageButtonList.GuiSlideEntry(112, I18n.format("createWorld.customize.custom.biomeDepthWeight"), false, this, 1, 20, field_175336_F.biomeDepthWeight), new GuiPageButtonList.GuiSlideEntry(113, I18n.format("createWorld.customize.custom.biomeDepthOffset"), false, this, 0, 20, field_175336_F.biomeDepthOffset), new GuiPageButtonList.GuiSlideEntry(114, I18n.format("createWorld.customize.custom.biomeScaleWeight"), false, this, 1, 20, field_175336_F.biomeScaleWeight), new GuiPageButtonList.GuiSlideEntry(115, I18n.format("createWorld.customize.custom.biomeScaleOffset"), false, this, 0, 20, field_175336_F.biomeScaleOffset)};
        GuiPageButtonList.GuiListEntry[] aguilistentry3 = new GuiPageButtonList.GuiListEntry[]{new GuiPageButtonList.GuiLabelEntry(400, I18n.format("createWorld.customize.custom.mainNoiseScaleX") + ":", false), new GuiPageButtonList.EditBoxEntry(132, String.format("%5.3f", field_175336_F.mainNoiseScaleX), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(401, I18n.format("createWorld.customize.custom.mainNoiseScaleY") + ":", false), new GuiPageButtonList.EditBoxEntry(133, String.format("%5.3f", new Object[]{Float.valueOf(field_175336_F.mainNoiseScaleY)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(402, I18n.format("createWorld.customize.custom.mainNoiseScaleZ") + ":", false), new GuiPageButtonList.EditBoxEntry(134, String.format("%5.3f", field_175336_F.mainNoiseScaleZ), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(403, I18n.format("createWorld.customize.custom.depthNoiseScaleX") + ":", false), new GuiPageButtonList.EditBoxEntry(135, String.format("%5.3f", new Object[]{Float.valueOf(field_175336_F.depthNoiseScaleX)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(404, I18n.format("createWorld.customize.custom.depthNoiseScaleZ") + ":", false), new GuiPageButtonList.EditBoxEntry(136, String.format("%5.3f", new Object[]{Float.valueOf(field_175336_F.depthNoiseScaleZ)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(405, I18n.format("createWorld.customize.custom.depthNoiseScaleExponent") + ":", false), new GuiPageButtonList.EditBoxEntry(137, String.format("%2.3f", new Object[]{Float.valueOf(field_175336_F.depthNoiseScaleExponent)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(406, I18n.format("createWorld.customize.custom.baseSize") + ":", false), new GuiPageButtonList.EditBoxEntry(138, String.format("%2.3f", new Object[]{Float.valueOf(field_175336_F.baseSize)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(407, I18n.format("createWorld.customize.custom.coordinateScale") + ":", false), new GuiPageButtonList.EditBoxEntry(139, String.format("%5.3f", new Object[]{Float.valueOf(field_175336_F.coordinateScale)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(408, I18n.format("createWorld.customize.custom.heightScale") + ":", false), new GuiPageButtonList.EditBoxEntry(140, String.format("%5.3f", new Object[]{Float.valueOf(field_175336_F.heightScale)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(409, I18n.format("createWorld.customize.custom.stretchY") + ":", false), new GuiPageButtonList.EditBoxEntry(141, String.format("%2.3f", new Object[]{Float.valueOf(field_175336_F.stretchY)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(410, I18n.format("createWorld.customize.custom.upperLimitScale") + ":", false), new GuiPageButtonList.EditBoxEntry(142, String.format("%5.3f", new Object[]{Float.valueOf(field_175336_F.upperLimitScale)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(411, I18n.format("createWorld.customize.custom.lowerLimitScale") + ":", false), new GuiPageButtonList.EditBoxEntry(143, String.format("%5.3f", new Object[]{Float.valueOf(field_175336_F.lowerLimitScale)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(412, I18n.format("createWorld.customize.custom.biomeDepthWeight") + ":", false), new GuiPageButtonList.EditBoxEntry(144, String.format("%2.3f", new Object[]{Float.valueOf(field_175336_F.biomeDepthWeight)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(413, I18n.format("createWorld.customize.custom.biomeDepthOffset") + ":", false), new GuiPageButtonList.EditBoxEntry(145, String.format("%2.3f", new Object[]{Float.valueOf(field_175336_F.biomeDepthOffset)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(414, I18n.format("createWorld.customize.custom.biomeScaleWeight") + ":", false), new GuiPageButtonList.EditBoxEntry(146, String.format("%2.3f", new Object[]{Float.valueOf(field_175336_F.biomeScaleWeight)}), false, isValidFloatStr), new GuiPageButtonList.GuiLabelEntry(415, I18n.format("createWorld.customize.custom.biomeScaleOffset") + ":", false), new GuiPageButtonList.EditBoxEntry(147, String.format("%2.3f", new Object[]{Float.valueOf(field_175336_F.biomeScaleOffset)}), false, isValidFloatStr)};
        guiPageButtonList = new GuiPageButtonList(mc, width, height, 32, height - 32, 25, this, aguilistentry, aguilistentry1, aguilistentry2, aguilistentry3);

        for (int i = 0; i < 4; ++i)
        {
            field_175342_h[i] = I18n.format("createWorld.customize.custom.page" + i);
        }

        func_175328_i();
    }

    public String func_175323_a()
    {
        return field_175336_F.toString().replace("\n", "");
    }

    public void func_175324_a(String p_175324_1_)
    {
        if (p_175324_1_ != null && p_175324_1_.length() != 0)
        {
            field_175336_F = ChunkGeneratorSettings.Factory.jsonToFactory(p_175324_1_);
        }
        else
        {
            field_175336_F = new ChunkGeneratorSettings.Factory();
        }
    }

    @Override
    public void setEntryValue(int i, String s)
    {
        float f = 0;

        try
        {
            f = Float.parseFloat(s);
        }
        catch (NumberFormatException numberformatexception)
        {
        }

        float f1 = 0;

        switch (i)
        {
            case 132:
                f1 = field_175336_F.mainNoiseScaleX = MathHelper.clamp(f, 1, 5000);
                break;
            case 133:
                f1 = field_175336_F.mainNoiseScaleY = MathHelper.clamp(f, 1, 5000);
                break;
            case 134:
                f1 = field_175336_F.mainNoiseScaleZ = MathHelper.clamp(f, 1, 5000);
                break;
            case 135:
                f1 = field_175336_F.depthNoiseScaleX = MathHelper.clamp(f, 1, 2000);
                break;
            case 136:
                f1 = field_175336_F.depthNoiseScaleZ = MathHelper.clamp(f, 1, 2000);
                break;
            case 137:
                f1 = field_175336_F.depthNoiseScaleExponent = MathHelper.clamp(f, 0.01F, 20);
                break;
            case 138:
                f1 = field_175336_F.baseSize = MathHelper.clamp(f, 1, 25);
                break;
            case 139:
                f1 = field_175336_F.coordinateScale = MathHelper.clamp(f, 1, 6000);
                break;
            case 140:
                f1 = field_175336_F.heightScale = MathHelper.clamp(f, 1, 6000);
                break;
            case 141:
                f1 = field_175336_F.stretchY = MathHelper.clamp(f, 0.01F, 50);
                break;
            case 142:
                f1 = field_175336_F.upperLimitScale = MathHelper.clamp(f, 1, 5000);
                break;
            case 143:
                f1 = field_175336_F.lowerLimitScale = MathHelper.clamp(f, 1, 5000);
                break;
            case 144:
                f1 = field_175336_F.biomeDepthWeight = MathHelper.clamp(f, 1, 20);
                break;
            case 145:
                f1 = field_175336_F.biomeDepthOffset = MathHelper.clamp(f, 0, 20);
                break;
            case 146:
                f1 = field_175336_F.biomeScaleWeight = MathHelper.clamp(f, 1, 20);
                break;
            case 147:
                f1 = field_175336_F.biomeScaleOffset = MathHelper.clamp(f, 0, 20);
        }

        if (f1 != f && f != 0)
        {
            ((GuiTextField) guiPageButtonList.getComponent(i)).setText(func_175330_b(i, f1));
        }

        ((GuiSlider) guiPageButtonList.getComponent(i - 132 + 100)).setSliderValue(f1, false);

        if (!field_175336_F.equals(field_175334_E))
        {
            field_175338_A = true;
        }
    }

    @Override
    public String getText(int i, String s, float f)
    {
        return s + ": " + func_175330_b(i, f);
    }

    private String func_175330_b(int i, float f)
    {
        switch (i)
        {
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 107:
            case 108:
            case 110:
            case 111:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 139:
            case 140:
            case 142:
            case 143:
                return String.format("%5.3f", f);
            case 105:
            case 106:
            case 109:
            case 112:
            case 113:
            case 114:
            case 115:
            case 137:
            case 138:
            case 141:
            case 144:
            case 145:
            case 146:
            case 147:
                return String.format("%2.3f", f);
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
            case 160:
            case 161:
            default:
                return String.format("%d", (int) f);
            case 162:
                if (f < 0)
                {
                    return I18n.format("gui.all");
                }
                else
                {
                    Biome biomegenbase;

                    if ((int) f >= Biome.getIdForBiome(Biomes.HELL))
                    {
                        biomegenbase = Biome.getBiome((int) f + 2);
                        return biomegenbase != null ? biomegenbase.getBiomeName() : "?";
                    }
                    else
                    {
                        biomegenbase = Biome.getBiome((int) f);
                        return biomegenbase != null ? biomegenbase.getBiomeName() : "?";
                    }
                }
        }
    }

    @Override
    public void setEntryValue(int p_175321_1_, boolean p_175321_2_)
    {
        switch (p_175321_1_)
        {
            case 148:
                field_175336_F.useCaves = p_175321_2_;
                break;
            case 149:
                field_175336_F.useDungeons = p_175321_2_;
                break;
            case 150:
                field_175336_F.useStrongholds = p_175321_2_;
                break;
            case 151:
                field_175336_F.useVillages = p_175321_2_;
                break;
            case 152:
                field_175336_F.useMineShafts = p_175321_2_;
                break;
            case 153:
                field_175336_F.useTemples = p_175321_2_;
                break;
            case 154:
                field_175336_F.useRavines = p_175321_2_;
                break;
            case 155:
                field_175336_F.useWaterLakes = p_175321_2_;
                break;
            case 156:
                field_175336_F.useLavaLakes = p_175321_2_;
                break;
            case 161:
                field_175336_F.useLavaOceans = p_175321_2_;
                break;
            case 210:
                field_175336_F.useMonuments = p_175321_2_;
        }

        if (!field_175336_F.equals(field_175334_E))
        {
            field_175338_A = true;
        }
    }

    @Override
    public void setEntryValue(int i, float f)
    {
        switch (i)
        {
            case 100:
                field_175336_F.mainNoiseScaleX = f;
                break;
            case 101:
                field_175336_F.mainNoiseScaleY = f;
                break;
            case 102:
                field_175336_F.mainNoiseScaleZ = f;
                break;
            case 103:
                field_175336_F.depthNoiseScaleX = f;
                break;
            case 104:
                field_175336_F.depthNoiseScaleZ = f;
                break;
            case 105:
                field_175336_F.depthNoiseScaleExponent = f;
                break;
            case 106:
                field_175336_F.baseSize = f;
                break;
            case 107:
                field_175336_F.coordinateScale = f;
                break;
            case 108:
                field_175336_F.heightScale = f;
                break;
            case 109:
                field_175336_F.stretchY = f;
                break;
            case 110:
                field_175336_F.upperLimitScale = f;
                break;
            case 111:
                field_175336_F.lowerLimitScale = f;
                break;
            case 112:
                field_175336_F.biomeDepthWeight = f;
                break;
            case 113:
                field_175336_F.biomeDepthOffset = f;
                break;
            case 114:
                field_175336_F.biomeScaleWeight = f;
                break;
            case 115:
                field_175336_F.biomeScaleOffset = f;
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
            case 155:
            case 156:
            case 161:
            case 188:
            default:
                break;
            case 157:
                field_175336_F.dungeonChance = (int) f;
                break;
            case 158:
                field_175336_F.waterLakeChance = (int) f;
                break;
            case 159:
                field_175336_F.lavaLakeChance = (int) f;
                break;
            case 160:
                field_175336_F.seaLevel = (int) f;
                break;
            case 162:
                field_175336_F.fixedBiome = (int) f;
                break;
            case 163:
                field_175336_F.biomeSize = (int) f;
                break;
            case 164:
                field_175336_F.riverSize = (int) f;
                break;
            case 165:
                field_175336_F.dirtSize = (int) f;
                break;
            case 166:
                field_175336_F.dirtCount = (int) f;
                break;
            case 167:
                field_175336_F.dirtMinHeight = (int) f;
                break;
            case 168:
                field_175336_F.dirtMaxHeight = (int) f;
                break;
            case 169:
                field_175336_F.gravelSize = (int) f;
                break;
            case 170:
                field_175336_F.gravelCount = (int) f;
                break;
            case 171:
                field_175336_F.gravelMinHeight = (int) f;
                break;
            case 172:
                field_175336_F.gravelMaxHeight = (int) f;
                break;
            case 173:
                field_175336_F.graniteSize = (int) f;
                break;
            case 174:
                field_175336_F.graniteCount = (int) f;
                break;
            case 175:
                field_175336_F.graniteMinHeight = (int) f;
                break;
            case 176:
                field_175336_F.graniteMaxHeight = (int) f;
                break;
            case 177:
                field_175336_F.dioriteSize = (int) f;
                break;
            case 178:
                field_175336_F.dioriteCount = (int) f;
                break;
            case 179:
                field_175336_F.dioriteMinHeight = (int) f;
                break;
            case 180:
                field_175336_F.dioriteMaxHeight = (int) f;
                break;
            case 181:
                field_175336_F.andesiteSize = (int) f;
                break;
            case 182:
                field_175336_F.andesiteCount = (int) f;
                break;
            case 183:
                field_175336_F.andesiteMinHeight = (int) f;
                break;
            case 184:
                field_175336_F.andesiteMaxHeight = (int) f;
                break;
            case 185:
                field_175336_F.coalSize = (int) f;
                break;
            case 186:
                field_175336_F.coalCount = (int) f;
                break;
            case 187:
                field_175336_F.coalMinHeight = (int) f;
                break;
            case 189:
                field_175336_F.coalMaxHeight = (int) f;
                break;
            case 190:
                field_175336_F.ironSize = (int) f;
                break;
            case 191:
                field_175336_F.ironCount = (int) f;
                break;
            case 192:
                field_175336_F.ironMinHeight = (int) f;
                break;
            case 193:
                field_175336_F.ironMaxHeight = (int) f;
                break;
            case 194:
                field_175336_F.goldSize = (int) f;
                break;
            case 195:
                field_175336_F.goldCount = (int) f;
                break;
            case 196:
                field_175336_F.goldMinHeight = (int) f;
                break;
            case 197:
                field_175336_F.goldMaxHeight = (int) f;
                break;
            case 198:
                field_175336_F.redstoneSize = (int) f;
                break;
            case 199:
                field_175336_F.redstoneCount = (int) f;
                break;
            case 200:
                field_175336_F.redstoneMinHeight = (int) f;
                break;
            case 201:
                field_175336_F.redstoneMaxHeight = (int) f;
                break;
            case 202:
                field_175336_F.diamondSize = (int) f;
                break;
            case 203:
                field_175336_F.diamondCount = (int) f;
                break;
            case 204:
                field_175336_F.diamondMinHeight = (int) f;
                break;
            case 205:
                field_175336_F.diamondMaxHeight = (int) f;
                break;
            case 206:
                field_175336_F.lapisSize = (int) f;
                break;
            case 207:
                field_175336_F.lapisCount = (int) f;
                break;
            case 208:
                field_175336_F.lapisCenterHeight = (int) f;
                break;
            case 209:
                field_175336_F.lapisSpread = (int) f;
        }

        if (i >= 100 && i < 116)
        {
            Gui gui = guiPageButtonList.getComponent(i - 32);

            if (gui != null) ((GuiTextField) gui).setText(func_175330_b(i, f));
        }

        if (!field_175336_F.equals(field_175334_E))
        {
            field_175338_A = true;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            switch (button.id)
            {
                case 300:
                    guiCreateDimension.chunkProviderSettingsJson = field_175336_F.toString();
                    mc.displayGuiScreen(guiCreateDimension);
                    break;

                case 301:
                    for (int i = 0; i < guiPageButtonList.getSize(); ++i)
                    {
                        GuiPageButtonList.GuiEntry guientry = guiPageButtonList.getListEntry(i);
                        Gui gui = guientry.getComponent1();

                        if (gui instanceof GuiButton)
                        {
                            GuiButton guibutton1 = (GuiButton) gui;

                            if (guibutton1 instanceof GuiSlider)
                            {
                                float f = ((GuiSlider) guibutton1).getSliderPosition() * (0.75F + random.nextFloat() * 0.5F) + (random.nextFloat() * 0.1F - 0.05F);
                                ((GuiSlider) guibutton1).setSliderPosition(MathHelper.clamp(f, 0, 1));
                            }
                            else if (guibutton1 instanceof GuiListButton)
                            {
                                ((GuiListButton) guibutton1).setValue(random.nextBoolean());
                            }
                        }

                        Gui gui1 = guientry.getComponent2();

                        if (gui1 instanceof GuiButton)
                        {
                            GuiButton guibutton2 = (GuiButton) gui1;

                            if (guibutton2 instanceof GuiSlider)
                            {
                                float f1 = ((GuiSlider) guibutton2).getSliderPosition() * (0.75F + random.nextFloat() * 0.5F) + (random.nextFloat() * 0.1F - 0.05F);
                                ((GuiSlider) guibutton2).setSliderPosition(MathHelper.clamp(f1, 0, 1));
                            }
                            else if (guibutton2 instanceof GuiListButton)
                            {
                                ((GuiListButton) guibutton2).setValue(random.nextBoolean());
                            }
                        }
                    }
                    return;

                case 302:
                    guiPageButtonList.previousPage();
                    func_175328_i();
                    break;

                case 303:
                    guiPageButtonList.nextPage();
                    func_175328_i();
                    break;

                case 304:
                    if (field_175338_A) func_175322_b(304);
                    break;

                case 305:
                    mc.displayGuiScreen(new GuiScreenCustomizeDimensionPresets(this));
                    break;

                case 306:
                    func_175331_h();
                    break;

                case 307:
                    field_175339_B = 0;
                    func_175331_h();
            }
        }
    }

    private void func_175326_g()
    {
        field_175336_F.setDefaults();
        func_175325_f();
    }

    private void func_175322_b(int i)
    {
        field_175339_B = i;
        func_175329_a(true);
    }

    private void func_175331_h() throws IOException
    {
        switch (field_175339_B)
        {
            case 300:
                actionPerformed((GuiListButton) guiPageButtonList.getComponent(300));
                break;
            case 304:
                func_175326_g();
        }

        field_175339_B = 0;
        field_175340_C = true;
        func_175329_a(false);
    }

    private void func_175329_a(boolean p_175329_1_)
    {
        guiButtonYes.visible = p_175329_1_;
        guiButtonNo.visible = p_175329_1_;
        guiButton1.enabled = !p_175329_1_;
        guiButton.enabled = !p_175329_1_;
        guiButton3.enabled = !p_175329_1_;
        guiButton4.enabled = !p_175329_1_;
        guiButton2.enabled = !p_175329_1_;
        guiButton7.enabled = !p_175329_1_;
    }

    private void func_175328_i()
    {
        guiButton3.enabled = guiPageButtonList.getPage() != 0;
        guiButton4.enabled = guiPageButtonList.getPage() != guiPageButtonList.getPageCount() - 1;
        field_175333_f = I18n.format("book.pageIndicator", new Object[]{Integer.valueOf(guiPageButtonList.getPage() + 1), Integer.valueOf(guiPageButtonList.getPageCount())});
        field_175335_g = field_175342_h[guiPageButtonList.getPage()];
        guiButton1.enabled = guiPageButtonList.getPage() != guiPageButtonList.getPageCount() - 1;
    }

    /**
     * Fired when a key is typed (except F11 who toggle full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);

        if (field_175339_B == 0)
        {
            switch (keyCode)
            {
                case 200:
                    func_175327_a(1);
                    break;
                case 208:
                    func_175327_a(-1);
                    break;
                default:
                    guiPageButtonList.onKeyPressed(typedChar, keyCode);
            }
        }
    }

    private void func_175327_a(float f)
    {
        Gui gui = guiPageButtonList.getFocusedControl();

        if (gui instanceof GuiTextField)
        {
            float f1 = f;

            if (GuiScreen.isShiftKeyDown())
            {
                f1 = f * 0.1F;

                if (GuiScreen.isCtrlKeyDown())
                {
                    f1 *= 0.1F;
                }
            }
            else if (GuiScreen.isCtrlKeyDown())
            {
                f1 = f * 10;

                if (GuiScreen.isAltKeyDown())
                {
                    f1 *= 10;
                }
            }

            GuiTextField guitextfield = (GuiTextField) gui;
            try
            {
                float f2 = Float.parseFloat(guitextfield.getText() + f1);
                int i = guitextfield.getId();
                String s = func_175330_b(i, f2);
                guitextfield.setText(s);
                setEntryValue(i, s);
            }
            catch (NumberFormatException e)
            {
            }
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (field_175339_B == 0 && !field_175340_C)
        {
            guiPageButtonList.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * Called when a mouse button is released.  Args : mouseX, mouseY, releaseButton
     */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (field_175340_C)
        {
            field_175340_C = false;
        }
        else if (field_175339_B == 0)
        {
            guiPageButtonList.mouseReleased(mouseX, mouseY, state);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        guiPageButtonList.drawScreen(mouseX, mouseY, partialTicks);
        int halfWidth = width >> 1;
        drawCenteredString(fontRenderer, field_175341_a, halfWidth, 2, 16777215);
        drawCenteredString(fontRenderer, field_175333_f, halfWidth, 12, 16777215);
        drawCenteredString(fontRenderer, field_175335_g, halfWidth, 22, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (field_175339_B != 0)
        {
            drawRect(0, 0, width, height, Integer.MIN_VALUE);

            drawHorizontalLine(halfWidth - 91, halfWidth + 90, 99, -2039584);
            drawHorizontalLine(halfWidth - 91, halfWidth + 90, 185, -6250336);
            drawVerticalLine(halfWidth - 91, 99, 185, -2039584);
            drawVerticalLine(halfWidth + 90, 99, 185, -6250336);

            GlStateManager.disableLighting();
            GlStateManager.disableFog();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexBuffer = tessellator.getBuffer();
            mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
            GlStateManager.color(1, 1, 1, 1);
            vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexBuffer.pos(halfWidth - 90, 185, 0).tex(0, 2.65625).color(64, 64, 64, 64).endVertex();
            vertexBuffer.pos(halfWidth + 90, 185, 0).tex(5.625, 2.65625).color(64, 64, 64, 64).endVertex();
            vertexBuffer.pos(halfWidth + 90, 100, 0).tex(5.625, 0).color(64, 64, 64, 64).endVertex();
            vertexBuffer.pos(halfWidth - 90, 100, 0).tex(0, 0).color(64, 64, 64, 64).endVertex();
            tessellator.draw();

            drawCenteredString(fontRenderer, I18n.format("createWorld.customize.custom.confirmTitle"), halfWidth, 105, 16777215);
            drawCenteredString(fontRenderer, I18n.format("createWorld.customize.custom.confirm1"), halfWidth, 125, 16777215);
            drawCenteredString(fontRenderer, I18n.format("createWorld.customize.custom.confirm2"), halfWidth, 135, 16777215);

            guiButtonYes.drawButton(mc, mouseX, mouseY, partialTicks);
            guiButtonNo.drawButton(mc, mouseX, mouseY, partialTicks);
        }
    }
}